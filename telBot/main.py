import asyncio
import json

import aiofiles
import sys
import logging
from os import getenv
from aiogram import Bot, Dispatcher, html, F
from aiogram.client.default import DefaultBotProperties
import aiohttp
from aiogram.enums import ParseMode
from aiogram.exceptions import TelegramBadRequest
from aiogram.filters import CommandStart
from aiogram.types import (
    Message,
    ReactionTypeEmoji,
    CallbackQuery,
    InlineKeyboardMarkup,
    InlineKeyboardButton,
    ReplyKeyboardMarkup,
    KeyboardButton,
    InlineQuery,
    InlineQueryResultArticle,
    InputTextMessageContent, reply_markup_union
)
TOKEN = getenv("BOT_TOKEN")
JAVA_SERVER = getenv("JAVA_SERVER")
dp = Dispatcher()

# ------------- Welcome Text -------------
with open("welcomeMessage.txt", "r", encoding="utf-8") as file:
    welcome_content = file.read()
# ------------- Connect to Spring Boot -------------

async def api_get(path: str, params: dict | None = None):
    async with aiohttp.ClientSession() as session:
        async with session.get(f"{JAVA_SERVER}{path}", params=params) as resp:
            resp.raise_for_status()
            return await resp.json()


async def api_post(path: str, data: dict):
    async with aiohttp.ClientSession() as session:
        async with session.post(f"{JAVA_SERVER}{path}", json=data) as resp:
            resp.raise_for_status()
            return await resp.json()

# ------------------ AI Handler ----------------------

async def ai_handler(message: Message, bot : Bot) -> None:
    content = message.caption or message.text or ""
    data = aiohttp.FormData()
    data.add_field("user_id", str(message.from_user.id))
    data.add_field("chat_id", str(message.chat.id))
    data.add_field("message_id",str(message.message_id))
    data.add_field("username",str(message.from_user.username))
    data.add_field(
        "content",
        content,
        content_type="text/plain"
    )
    if message.photo:
        photo = message.photo[-1]
        photo_file = await bot.download(photo.file_id)
        data.add_field("file",photo_file.read() ,filename=f"{photo.file_unique_id}.jpg",content_type="image/jpeg")
        data.add_field("msg_type","PHOTO")
    else:
        data.add_field("msg_type","TEXT")

    async with aiohttp.ClientSession() as session:
        try:
            async with session.post(JAVA_SERVER +"api/bot/multifile", data=data) as response:
                if response.status == 200:
                    result = await response.json()
                    reply_text = result.get("reply","پیام دریافت شد")
                    await message.answer(reply_text)
                else:
                    error_text = await response.text()
                    logging.error(f"Java server error: status:{response.status}, error_text:'{error_text}'")
                    await message.answer("خطا در ارتباط با سرور")
        except Exception as e:
            logging.error(f"Error sending to Java server{e}")
            await message.answer("سرور در دسترس نیست")


# ------------------- Keyboards ----------------------
def main_menu() -> ReplyKeyboardMarkup:
    return ReplyKeyboardMarkup(
        keyboard=[
            [KeyboardButton(text="لیست دسته بندی ها")],
            [KeyboardButton(text="جستجو")],
        ],
        resize_keyboard=True,
    )


def categories_keyboard(category: list[dict]) -> InlineKeyboardMarkup:

    rows = [
        [InlineKeyboardButton(text=key["name"], callback_data=f"child:{key['id']}")]
        for key in category
    ]
    return InlineKeyboardMarkup(inline_keyboard=rows)

def categories_child_keyboard(category: list[dict], id) -> InlineKeyboardMarkup:

    rows = [
        [InlineKeyboardButton(text=key["name"], callback_data=f"products:{key['id']}:{id}")]
        for key in category
    ]
    rows.append([InlineKeyboardButton(text="⬅️ بازگشت", callback_data="لیست دسته بندی ها")])
    return InlineKeyboardMarkup(inline_keyboard=rows)

def products_keyboard(products: list[dict],child_id) -> InlineKeyboardMarkup:

    rows = [
        [InlineKeyboardButton(text=key["name"], callback_data=f"child:{key['id']}")]
        for key in products
    ]
    rows.append([InlineKeyboardButton(text="⬅️ بازگشت", callback_data=f"child:{child_id}")])
    return InlineKeyboardMarkup(inline_keyboard=rows)
# ------------------- Handler ----------------------

@dp.message(CommandStart())
async def command_start_handler(message: Message) -> None:
    await message.react(reaction=[ReactionTypeEmoji(emoji="❤️")])
    await message.answer(welcome_content,reply_markup=main_menu())

@dp.message(F.text == "جستجو")
async def shearch_handler(message: Message) -> None:
    await message.reply("کلمه ای که می خوای جستجو بشه رو بفرست👇")

@dp.message(F.text == "لیست دسته بندی ها")
async def show_all_category(message: Message):
    try:
        category = await api_get("category/tree")
    except Exception:
        await message.answer("خطا در ارتباط با سرور")
        return
    await message.answer("دسته بندی ها:", reply_markup=categories_keyboard(category))

@dp.callback_query(F.data.startswith("لیست دسته بندی ها"))
async def show_all_category(callback: CallbackQuery):
    try:
        category = await api_get("category/tree")
    except Exception:
        await callback.message.answer("خطا در ارتباط با سرور")
        await callback.answer()
        return
    try:
        await callback.message.edit_text("دسته بندی ها:", reply_markup=categories_keyboard(category))
    except TelegramBadRequest as e:
        if "message is not modified" not in str(e):
            raise
    await callback.answer()


@dp.callback_query(F.data.startswith("child:"))
async def child_detail(callback: CallbackQuery):
    category_id = callback.data.split(":")[1]
    try:
        categories = await api_get(f"category/{category_id}/children")
    except Exception:
        await callback.message.answer("خطا در ارتباط با سرور")
        await callback.answer()
        return
    try:
        await callback.message.edit_text("دسته بندی ها:", reply_markup=categories_child_keyboard(categories,category_id))
    except TelegramBadRequest as e:
        if "message is not modified" not in str(e):
            raise
    await callback.answer()

@dp.callback_query(F.data.startswith("products:"))
async def product_detail(callback: CallbackQuery):
    child_id = callback.data.split(":")[1]
    parent_id = callback.data.split(":")[2]
    try:
        products = await api_get(f"category/{child_id}/products")
    except Exception:
        await callback.message.answer("خطا در ارتباط با سرور")
        await callback.answer()
        return
    try:
        await callback.message.edit_text("محصولات این دسته بندی:", reply_markup=products_keyboard(products,parent_id))
    except TelegramBadRequest as e:
        if "message is not modified" not in str(e):
            raise
    await callback.answer()

#------------------ search ---------------------

@dp.inline_query()
async def inline_search_handler(query: InlineQuery):
    text = query.query.strip()
    if text.startswith("search:"):
        keyword = text.removeprefix("search:").strip()
    else:
        keyword = text
    if not text:
        await query.answer([],cache_time=1)
        return
    try:
        products = await api_get(f"product/search/{keyword}")
    except Exception as e:
        await query.answer([],cache_time=1)
        return
    results = []
    for p in products[:20]:
        results.append(
            InlineQueryResultArticle(
                id=str(p["id"]),
                title=p["name"],
                description=f"{p['shortDesc']} | {p['price']} تومان ",
                input_message_content=InputTextMessageContent(
                    message_text=f"{p['name']}\n {p['shortDesc']} قیمت : {p['price']} تومان"
                ),
                thumbnail_url=None
            )
        )

        await query.answer(results, cache_time=5, is_personal=True)


@dp.message(F.text)
async def ask_to_search(message: Message):
    text = message.text.strip()

    keyboard = InlineKeyboardMarkup(
        inline_keyboard=[
            [
                InlineKeyboardButton(
                text="جستجو کنم برات؟؟",
                switch_inline_query_current_chat=f"search: {text}"
                ),
                InlineKeyboardButton(
                    text = "ارسال کن به AI",
                    callback_data="send_ai"
                )
            ]
        ]
    )
    await  message.reply(f"می خوام {text} رو جستجو کنم؟", reply_markup= keyboard)


@dp.message(F.photo)
async def photo_handler(message: Message, bot : Bot):
    await ai_handler(message, bot)

@dp.callback_query(F.data == "send_ai")
async def send_ai_handler(callback: CallbackQuery, bot : Bot):
    await callback.answer()

    text = callback.message.reply_to_message
    if text is None:
        await callback.message.answer("پیام دریافت نشد ، دوباره بفرست")
        return
    await callback.message.delete()
    await ai_handler(text, bot)

async def main() -> None:
    bot = Bot(token=str(TOKEN), default=DefaultBotProperties(parse_mode=ParseMode.HTML))
    await dp.start_polling(bot)

if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO, stream=sys.stdout)
    asyncio.run(main())



