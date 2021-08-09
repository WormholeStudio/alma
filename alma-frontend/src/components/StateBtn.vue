<template>
  <div class="btn-state">
    <el-button
      size="mini"
      v-if="currentState"
      :type="currentState.type"
      :icon="currentState.icon"
      :disabled="currentState.disabled"
      v-loading="submiting"
      @click="() => currentState.onClick && currentState.onClick()"
    >
      {{ currentState.label }}
    </el-button>
  </div>
</template>

<script>
  import { Claim_Airdrop } from 'utils/Provider';
  import { mapGetters } from 'vuex';

  export default {
    props: {
      data: { type: Object, default: () => {} },
    },
    data() {
      return {
        submiting: false,
        btnConfigList: {
          WAITING: {
            label: '尚未开始',
            disabled: true,
          },
          JOIN: {
            label: '参加',
            icon: 'el-icon-s-flag',
            type: 'primary',
            onClick: () => {
              if (this.data?.participate_url) {
                window.open(this.data.participate_url, '_blank');
              }
            },
          },
          SENDING: {
            label: '发放中',
            icon: 'el-icon-timer',
            disabled: true,
            type: 'warning',
          },
          UNCLAIMED: {
            label: '领取',
            type: 'success',
            icon: 'el-icon-document-copy',
            onClick: () => this.claimAirdrop(),
          },
          CLAIMED: {
            label: '已领取',
            disabled: true,
            icon: 'el-icon-document-checked',
          },
          FINISH: {
            label: '活动结束',
            disabled: true,
            icon: 'el-icon-finished',
          },
        },
      };
    },
    computed: {
      currentState() {
        return this.btnConfigList[this.data?.btn_state];
        // return this.btnConfigList['UNCLAIMED'];
      },
      ...mapGetters(['$accountHash']),
    },
    methods: {
      async claimAirdrop() {
        try {
          if (this.submiting) return;
          this.submiting = true;

          const { record, token_identifier, owner_address } = this.data;
          const txn = await Claim_Airdrop({
            ...record,
            token_identifier,
            owner_address,
          });

          const { airdrop_id, root } = record;

          this.$service
            .V1AirdropClaim({
              address: this.$accountHash,
              airdrop_id,
              root,
              txn,
            })
            .then((res) => {
              this.$message.success('claim done');
              this.$emit('success');
            })
            .finally(() => {
              this.submiting = false;
            });
        } catch (err) {
          this.submiting = false;
        }
      },
    },
  };
</script>

<style lang="less" scoped>
  .btn-state {
  }
</style>
