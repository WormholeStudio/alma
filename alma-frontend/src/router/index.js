import Vue from 'vue';
import VueRouter from 'vue-router';

Vue.use(VueRouter);

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import(/* webpackChunkName: "home" */ '../views/Home.vue'),
  },
  {
    path: '/send-airdrop',
    name: 'SendAirDrop',
    component: () => import(/* webpackChunkName: "send-air-drop" */ '../views/SendAirDrop.vue'),
  },
  {
    path: '*',
    redirect: {
      path: '/',
    },
  },
];

const router = new VueRouter({
  mode: 'hash',
  base: process.env.BASE_URL,
  routes,
});

export default router;