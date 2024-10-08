<script setup lang="ts">
const props = defineProps<{
  value: number
  disabled: boolean
  addWager: (value: number) => {}
}>()

const color = (() => {
  switch (props.value) {
    case 1:
      return '#e4d16a'
    case 5:
      return '#f48059'
    case 10:
      return '#7eb6c2'
    case 25:
      return '#5cad7b'
    default:
      return '#4b4b8b'
  }
})()

const onClick = () => {
  if (!props.disabled) props.addWager(props.value)
}
</script>

<template>
  <div class="chip" :class="{ disabled }" @click.prevent="onClick()" @mousedown.prevent="() => {}">
    <span>{{ value }}</span>
  </div>
</template>

<style lang="scss" scoped>
.chip {
  background-color: v-bind(color);
  height: 70px;
  width: 70px;
  border-radius: 50%;
  border: 4px dashed #1b3f5b;
  transition: all 0.25s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-family: 'Lato', sans-serif;
  font-size: 20px;
  margin: 0px 8px;
}

.chip.disabled {
  opacity: 50%;
}

.chip:hover:not(.disabled) {
  opacity: 75%;
  cursor: pointer;
}

div span {
  height: 120px;
  width: 120px;
  border-radius: 50%;
  text-align: center;
  line-height: 120px;
}
</style>
