import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/store/modules/user'

function checkPermission(el: HTMLElement, binding: DirectiveBinding<string[]>) {
  const { value } = binding

  if (value && value instanceof Array && value.length > 0) {
    const userStore = useUserStore()
    const permissions = userStore.permissions
    const allPermission = '*:*:*'
    const hasPermission = permissions.some((perm) => {
      return allPermission === perm || value.includes(perm)
    })
    if (!hasPermission) {
      el.parentNode?.removeChild(el)
    }
  } else {
    throw new Error('v-hasPermi requires a non-empty array value, e.g. v-hasPermi="[\'system:user:add\']"')
  }
}

export const hasPermi: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string[]>) {
    checkPermission(el, binding)
  },
  updated(el: HTMLElement, binding: DirectiveBinding<string[]>) {
    checkPermission(el, binding)
  },
}
