<template>
  <div class="connect-wallet">
    <div class="btn" @click="connectWallet">{{ btnText }}</div>

    <el-dialog
      custom-class="cute-dialog"
      width="80%"
      :visible.sync="showAccountDialog"
      title="你的钱包地址"
      append-to-body
    >
      <p class="account-address">{{ $accountHash }}</p>
      <p class="account-btn"><el-button type="text" @click="logout">退出</el-button></p>
    </el-dialog>
  </div>
</template>

<script>
  import { mapGetters, mapActions } from 'vuex';
  import StarMaskOnboarding from '@starcoin/starmask-onboarding';

  const CONNECT_TEXT = 'Connect';
  const LS_KEY_NAME = 'connectorId';

  export default {
    data() {
      return {
        btnText: CONNECT_TEXT,
        onBoarding: new StarMaskOnboarding(),
        showAccountDialog: false,
      };
    },
    watch: {
      $accountHash() {
        if (this.$accountHash === '') this.btnText = CONNECT_TEXT;
      },
    },
    computed: {
      isStarMaskInstalled() {
        return StarMaskOnboarding.isStarMaskInstalled();
      },
      ...mapGetters(['$accountHash']),
    },
    beforeDestroy() {
      if (window.starcoin && window.starcoin.off) {
        window.starcoin.off('accountsChanged', this.handleAccountChanged);
      }
    },
    created() {
      if (window.starcoin && window.starcoin.on) {
        window.starcoin.on('accountsChanged', this.handleAccountChanged);
      }
      // 如果之前没退出
      if (window.localStorage.getItem(LS_KEY_NAME)) {
        this.requestAccountFromSTC();
      }
    },
    methods: {
      /**
       * Save Account Hash
       */
      handleAccountChanged(accounts = []) {
        if (accounts.length > 0) {
          this.$updateAccountHash(accounts[0] || '');
          this.btnText = this.shortCutOfAccountHash(this.$accountHash);
          window.localStorage.setItem(LS_KEY_NAME, 'stc');
        }
      },
      /**
       * short cut for hash
       */
      shortCutOfAccountHash(hash) {
        return hash.replace(/^0x\w{4}(.*)\w{4}$/, (match, p1, offset, string) => {
          return string.replace(p1, '...');
        });
      },
      /**
       * Request Account
       */
      requestAccountFromSTC() {
        if (this.isStarMaskInstalled) {
          window.starcoin
            .request({
              method: 'stc_requestAccounts',
            })
            .then(this.handleAccountChanged)
            .catch((err) => {
              this.$message.error(err.message);
            });

          return Promise.resolve();
        }
        return Promise.reject();
      },
      /**
       * Connect Btn Click Handler
       */
      connectWallet() {
        if (this.$accountHash) {
          this.showAccountDialog = true;
        } else {
          this.requestAccountFromSTC().catch(() => {
            this.onBoarding.startOnboarding();
          });
        }
      },
      /**
       * Remove record
       */
      logout() {
        this.$updateAccountHash('');
        window.localStorage.removeItem(LS_KEY_NAME);
        this.showAccountDialog = false;
      },
      ...mapActions(['$updateAccountHash']),
    },
  };
</script>

<style lang="less" scoped>
  .connect-wallet {
    .btn {
      border: 2px solid var(--mainTextColor);
      border-radius: 0.5rem;
      padding: 0.2rem 0.5rem;
      cursor: pointer;
      font-weight: 600;

      &:hover {
        opacity: 0.8;
      }
    }
  }

  .account-address {
    font-weight: 600;
    line-height: 1.5;
    font-size: 24px;
    text-align: center;
    color: #280d5f;
  }

  .account-btn {
    text-align: center;
    margin-top: 10px;
  }

  @media screen and (max-width: 768px) {
    .connect-wallet {
      font-size: 12px;
    }
  }
</style>
