<template>
  <div class="alma-header">
    <div class="container">
      <div class="logo">Alma</div>
      <div class="nav">
        <div class="nav-item" :class="{ active: $route.name === 'Home' }" @click="jump('Home')">
          首页
        </div>
        <div
          class="nav-item"
          :class="{ active: $route.name === 'SendAirDrop' }"
          @click="jump('SendAirDrop')"
        >
          创建空投
        </div>
      </div>
      <div class="op">
        <button @click="signUp" class="sign-up-btn">Sign Up</button>
        <connect-wallet-btn></connect-wallet-btn>
      </div>
    </div>
  </div>
</template>

<script>
  import { GetSign, GetPublicKey } from 'utils/Provider';
  import ConnectWalletBtn from 'comp/ConnectWalletBtn';
  import { mapGetters } from 'vuex';

  export default {
    data() {
      return {};
    },
    components: {
      ConnectWalletBtn,
    },
    watch: {},
    computed: {
      ...mapGetters(['$accountHash']),
    },
    mounted() {
      // 切链刷新页面
      if (window.starcoin) {
        window.starcoin.on('chainChanged', () => window.location.reload());
      }
    },
    methods: {
      /**
       * 跳转
       */
      jump(routerName) {
        if (this.$route.name !== routerName) {
          this.$router.push({ name: routerName });
        }
      },
      async signUp() {
        if (this.$accountHash) {
          const publicKey = await GetPublicKey(this.$accountHash);
          const { nonce, message } = await this.$service.V1AuthNonce({
            address: this.$accountHash,
            publicKey,
          });
          const sign_message = await GetSign({ account: this.$accountHash, nonce, message });
          const ret = await this.$service.V1AuthToken({
            address: this.$accountHash,
            sign_message,
          });

          const LS_TOKEN_NAME = 'Alma_token';
          const tokenObj = {};
          tokenObj[this.$accountHash] = {
            ...ret,
            create_at: Date.now(),
          };
          window.localStorage.setItem(LS_TOKEN_NAME, JSON.stringify(tokenObj));
          console.log(nonce, ret);
        } else {
          this.$message.error('链接钱包先');
        }
      },
    },
  };
</script>

<style lang="less" scoped>
  .alma-header {
    width: 100%;
    background-color: var(--panelBgColor);
    display: flex;
    justify-content: center;

    .container {
      height: 56px;
      display: flex;
      align-items: center;

      .logo {
        font-size: 30px;
        font-weight: bolder;
      }

      .nav {
        display: flex;
        margin: 0 2rem;
        font-size: 14px;

        .nav-item {
          margin-right: 1rem;
          cursor: pointer;
          font-weight: 800;

          &:hover,
          &.active {
            color: var(--mainTextHoverColor);
          }
        }
      }

      .op {
        margin-left: auto;
        display: flex;
        justify-content: space-between;
        width: 220px;

        .sign-up-btn {
          padding: 5px 10px;
        }
      }
    }
  }
</style>
