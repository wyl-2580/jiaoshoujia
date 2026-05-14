import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import '@/assets/styles/index.scss'
import { hasPermi } from '@/directive/permission'
import i18n, { getLocale } from '@/i18n'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

const elementLocales: Record<string, any> = { 'zh-cn': zhCn, 'en': en }

app.use(createPinia())
app.use(router)
app.use(i18n)
app.use(ElementPlus, { locale: elementLocales[getLocale()] || zhCn })
app.directive('hasPermi', hasPermi)
app.mount('#app')
