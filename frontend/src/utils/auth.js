const TOKEN_KEY = 'plancraft_token'
const USER_KEY = 'plancraft_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function getUserInfo() {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function setUserInfo(info) {
  localStorage.setItem(USER_KEY, JSON.stringify(info))
}
