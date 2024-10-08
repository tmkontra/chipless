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
  margin: 0px 0px;
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

<!-- 
On Tuesday October 8, 2024
Chip element adapted from https://codepen.io/yakovd33/pen/jbbbPM:

Copyright (c) 2024 by yakovshitrit (https://codepen.io/yakovd33/pen/jbbbPM)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
-->
