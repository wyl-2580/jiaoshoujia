import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token'
const RefreshTokenKey = 'Refresh-Token'

export function getToken(): string | undefined {
  return Cookies.get(TokenKey)
}

export function setToken(token: string): void {
  Cookies.set(TokenKey, token)
}

export function removeToken(): void {
  Cookies.remove(TokenKey)
}

export function getRefreshToken(): string | undefined {
  return Cookies.get(RefreshTokenKey)
}

export function setRefreshToken(token: string): void {
  Cookies.set(RefreshTokenKey, token)
}

export function removeRefreshToken(): void {
  Cookies.remove(RefreshTokenKey)
}
