import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token'
const RefreshTokenKey = 'Refresh-Token'

const cookieOptions: Cookies.CookieAttributes = {
  sameSite: 'Strict' as const,
  secure: window.location.protocol === 'https:',
}

export function getToken(): string | undefined {
  return Cookies.get(TokenKey)
}

export function setToken(token: string): void {
  Cookies.set(TokenKey, token, { ...cookieOptions })
}

export function removeToken(): void {
  Cookies.remove(TokenKey)
}

export function getRefreshToken(): string | undefined {
  return Cookies.get(RefreshTokenKey)
}

export function setRefreshToken(token: string): void {
  Cookies.set(RefreshTokenKey, token, { ...cookieOptions, expires: 7 })
}

export function removeRefreshToken(): void {
  Cookies.remove(RefreshTokenKey)
}
