import Vue from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';

/**
 * Import Component of Element-ui
 */
import {
  Table,
  TableColumn,
  Pagination,
  Loading,
  Button,
  Message,
  MessageBox,
  Dialog,
  Upload,
  Form,
  FormItem,
  Input,
  Select,
  Option,
  DatePicker,
} from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';

[
  Table,
  TableColumn,
  Pagination,
  Button,
  Message,
  Dialog,
  Upload,
  Form,
  FormItem,
  Input,
  Select,
  Option,
  DatePicker,
].forEach((Comp) => {
  Vue.component(Comp.name, Comp);
});
Vue.use(Loading);
Vue.prototype.$message = Message;
Vue.prototype.$confirm = MessageBox.confirm;

import dayjs from 'dayjs';
Vue.filter('day', (input) => {
  return dayjs(input).format('YYYY年MM月DD日');
});

/**
 * Setting Service
 */
import ZeesuuService from '@zeesuu/service';
import axios from './utils/axios';
import APIs from 'config/APIs';
Vue.use(ZeesuuService, {
  apis: APIs,
  $http: axios,
  debug: process.env.NODE_ENV === 'development',
});

/**
 * Initial App
 */
Vue.config.productionTip = false;

/**
 * Import Global Style
 */
import './assets/global.less';

new Vue({
  router,
  store,
  render: (h) => h(App),
}).$mount('#app');
