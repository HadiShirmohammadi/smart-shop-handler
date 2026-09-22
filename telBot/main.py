import asyncio
import aiofiles
import sys
import logging
from os import getenv

from aiogram import Bot, Dispatcher, html
from aiogram.client.default import DefaultBotProperties
import aiohttp
from aiogram.enums import ParseMode
from aiogram.filters import CommandStart
from aiogram.types import Message, ReactionTypeEmoji

TOKEN = getenv("BOT_TOKEN")
JAVA_SERVER = getenv("JAVA_SERVER")
dp = Dispatcher()


@dp.message(CommandStart())
async def command_start_handler(message: Message) -> None:
    await message.react(reaction=[ReactionTypeEmoji(emoji="❤️")])
    async with aiofiles.open("welcomeMessage.txt", mode="r", encoding="utf-8") as file:
        welcome_content = await file.read()
    await message.answer(welcome_content)

@dp.message()
async def ai_handler(message: Message, bot : Bot) -> None:
    data = aiohttp.FormData()
    data.add_field("user_id", str(message.from_user.id))
    data.add_field("chat_id", str(message.chat.id))
    data.add_field("message_id",str(message.message_id))
    data.add_field("content",message.caption or message.text or "")
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



async def main() -> None:
    bot = Bot(token=str(TOKEN), default=DefaultBotProperties(parse_mode=ParseMode.HTML))
    await dp.start_polling(bot)

if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO, stream=sys.stdout)
    asyncio.run(main())