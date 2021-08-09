<template>
  <div id="app">
    <alma-header></alma-header>
    <router-view />
  </div>
</template>

<script>
  import AlmaHeader from './components/AlmaHeader.vue';
  export default {
    components: {
      AlmaHeader,
    },
  };
</script>

<style>
  #app {
    min-height: 100vh;
    font-family: Inter, -apple-system, BlinkMacSystemFont, 'segoe ui', Roboto, Helvetica, Arial,
      sans-serif;
  }
</style>
