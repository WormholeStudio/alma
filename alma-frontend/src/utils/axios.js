import axios from 'axios';
import { Message } from 'element-ui';
const R = axios.create({
  baseURL: process.env.VUE_APP_BASE_URL,
});

/**
 * 请求拦截器
 */
R.interceptors.request.use((_) => {
  const config = _;
  const IS_GET = config.method === 'get';
  const data = (IS_GET ? config.params : config.data) || {};

  if (IS_GET) {
    data['_t'] = Date.now();
    config.params = data;
  }

  return {
    ...config,
  };
});

/**
 * 响应拦截器
 */
R.interceptors.response.use(
  (response) => {
    if (response.data?.hasOwnProperty('is_success') && !response.data.is_success) {
      Message.error(`${response.config.url} Occured An Error!`);
      return Promise.reject(response.data);
    }
    return Promise.resolve(response.data);
  },
  (error) => {
    return Promise.reject(error.response.data);
  },
);

export default R;
