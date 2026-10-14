export const SESSION_KEY = 'gather-session';
export function readSession() {
  try { return JSON.parse(sessionStorage.getItem(SESSION_KEY)) || null; } catch { return null; }
}
export function saveSession(value) {
  try { value ? sessionStorage.setItem(SESSION_KEY, JSON.stringify(value)) : sessionStorage.removeItem(SESSION_KEY); } catch { /* Memory session still works when storage is unavailable. */ }
}
export class ApiError extends Error {
  constructor(message, status) { super(message); this.status = status; }
}
export async function request(path, { token, body, method = 'GET', signal } = {}) {
  const multipart = body instanceof FormData;
  let response;
  try {
    response = await fetch(`${(import.meta.env.VITE_API_BASE_URL || '/backend').replace(/\/$/, '')}${path}`, {
      method, signal,
      headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}), ...(body && !multipart ? { 'Content-Type': 'application/json' } : {}) },
      body: body ? (multipart ? body : JSON.stringify(body)) : undefined,
    });
  } catch (error) {
    if (error.name === 'AbortError') throw error;
    throw new ApiError('Cannot reach the server. Check that the Java backend is running and the backend URL is correct.', 0);
  }
  const text = await response.text();
  let data = text;
  if (text) { try { data = JSON.parse(text); } catch { /* Some endpoints return plain text. */ } }
  if (!response.ok) {
    const fallback = response.status === 401 ? 'Your session has expired. Please sign in again.' : response.status === 403 ? 'Access denied. Sign in with an account that has permission.' : `Server request failed (${response.status}).`;
    throw new ApiError((typeof data === 'string' && data && !data.startsWith('<') ? data : data?.message || data?.detail) || fallback, response.status);
  }
  return data || null;
}
