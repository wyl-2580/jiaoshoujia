import { createI18n } from 'vue-i18n'
import zhCn from './lang/zh-cn'
import en from './lang/en'

const storedLang = localStorage.getItem('locale') || 'zh-cn'

const i18n = createI18n({
  legacy: false,
  locale: storedLang,
  fallbackLocale: 'zh-cn',
  messages: {
    'zh-cn': zhCn,
    'en': en,
  },
})

export function setLocale(locale: string) {
  ;(i18n.global.locale as any).value = locale
  localStorage.setItem('locale', locale)
  document.querySelector('html')?.setAttribute('lang', locale)
}

export function getLocale(): string {
  return (i18n.global.locale as any).value
}

export default i18n
