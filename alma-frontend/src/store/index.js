import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    accountHash: '',
  },
  getters: {
    $accountHash: (state) => state.accountHash,
  },
  mutations: {
    UPDATE_ACCOUNT_HASH(state, payload) {
      state.accountHash = payload;
    },
  },
  actions: {
    $updateAccountHash({ commit }, payload) {
      commit('UPDATE_ACCOUNT_HASH', payload);
    },
  },
});
