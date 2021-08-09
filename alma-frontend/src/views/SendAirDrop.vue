<template>
  <div class="container send-air-drop">
    <div class="top-panel">
      <el-button type="success" icon="el-icon-upload" @click="openCreateAirdropDialog">
        创建空投
      </el-button>
    </div>
    <el-table stripe :data="airDropData" empty-text=" " v-loading="tableLoading">
      <el-table-column label="Project" prop="name"></el-table-column>
      <el-table-column
        label="Number of Winners"
        align="right"
        prop="winners_count"
      ></el-table-column>
      <el-table-column
        label="Total Airdrop Amount"
        align="right"
        prop="total_amount"
      ></el-table-column>
      <el-table-column label="Ends in" align="right" prop="end_at" width="130">
        <template slot-scope="scope">
          <div v-if="scope.row.end_at">
            {{ scope.row.end_at | day }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template slot-scope="scope">
          <div v-if="isDraft(scope.row.draft_state)">
            <el-button type="text" size="mini" @click="pub(scope.row)">发布</el-button>
            <el-button type="text" style="color: red" size="mini" @click="del(scope.row)">
              删除
            </el-button>
          </div>
          <el-button type="text" size="mini" v-else @click="send(scope.row)">发放</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Add Dialog -->
    <create-air-drop-dialog
      :visible.sync="createAirDropDialogShow"
      @done="getMyAirDrop"
    ></create-air-drop-dialog>

    <!-- Pub Dialog -->
    <el-dialog
      title="发布"
      :close-on-click-modal="false"
      :visible.sync="pubDialogShow"
      @close="pubDialogClose"
    >
      <p><input type="file" ref="fileInput" @change="inputFileChange" /></p>
      <p>
        <el-button size="mini" @click="pubWithCSV" v-loading="pubingWithCSV" :disabled="!csvFile">
          发布
        </el-button>
      </p>
    </el-dialog>
  </div>
</template>

<script>
  import BigNumber from 'bignumber.js';
  import CreateAirDropDialog from 'comp/CreateAirDropDialog';
  import { mapGetters, state } from 'vuex';
  import { Pub_Token_Merkle } from 'utils/Provider';

  export default {
    data() {
      return {
        airDropData: [],
        tableLoading: false,
        createAirDropDialogShow: false,
        pubDialogShow: false,
        pubAirDropId: '',
        csvFile: null,
        pubingWithCSV: false,
      };
    },
    components: {
      CreateAirDropDialog,
    },
    watch: {
      $accountHash() {
        if (this.$accountHash) {
          this.getMyAirDrop();
        } else {
          this.airDropData = [];
        }
      },
    },
    computed: {
      ...mapGetters(['$accountHash']),
    },
    mounted() {
      if (this.$accountHash) {
        this.getMyAirDrop();
      }
    },
    methods: {
      /**
       * 获取我的空投
       */
      getMyAirDrop() {
        if (!this.$accountHash) {
          return this.$message.error('请先连接钱包!');
        }

        this.$service
          .V1AirdropList({
            filter: 'MINE',
            address: this.$accountHash,
          })
          .then((res) => {
            this.airDropData = res;
          });
      },
      /**
       * 打开创建空投的对话框
       */
      openCreateAirdropDialog() {
        this.createAirDropDialogShow = true;
      },
      /**
       * 发布空投
       */
      pub(row) {
        if (row.id) {
          this.$confirm(`确认发布 ${row.name} 吗`, '发布空投')
            .then(() => {
              return this.$service.V1AirdropPublish({
                airdrop_id: row.id,
              });
            })
            .then(() => {
              this.$message.success('发布空投成功!');
              this.getMyAirDrop();
            });
        }
      },
      /**
       * 发放空投
       */
      send(row) {
        this.pubDialogShow = true;
        this.pubAirDropId = row.id;
      },
      /**
       * 删除一个空投
       */
      del(row) {
        if (row.id) {
          this.$confirm(`确认删除 ${row.name} 吗`, '删除空投')
            .then(() => {
              return this.$service.V1AirdropDelete({
                airdrop_id: row.id,
              });
            })
            .then(() => {
              this.$message.success('删除空投成功!');
              this.getMyAirDrop();
            });
        }
      },
      /**
       * 上传文件修改回调
       */
      inputFileChange(evt) {
        this.csvFile = evt.target.files[0] || {};
      },
      /**
       * 通过csv文件发放空投
       */
      pubWithCSV() {
        if (this.pubingWithCSV) return;
        this.pubingWithCSV = true;

        const formData = new FormData();
        formData.append('airdrop_id', this.pubAirDropId);
        formData.append('file', this.csvFile);
        this.$service
          .V1AirdropSend(formData, {
            headers: {
              'Content-Type': 'multipart/form-data',
            },
          })
          .then(async (res) => {
            this.pubDialogClose();
            const params = {
              account: this.$accountHash,
              airdrop_id: res.airdrop_id,
              merkle_root: res.api_merkle_tree.root,
              token_amounts: res.api_merkle_tree.proofs.reduce(
                (ret, current) =>
                  new BigNumber(ret).plus(new BigNumber(current.amount).toNumber()).toNumber(),
                0,
              ),
              leafs: res.api_merkle_tree.proofs.length,
              token_identifier: res.token_identifier,
            };

            // 调用合约
            const txn = await Pub_Token_Merkle(params);
            // 发放空投
            return this.$service.V1AirdropCommit({
              airdrop_id: res.airdrop_id,
              root: res.api_merkle_tree.root,
              txn,
            });
          })
          .then(() => {
            this.pubDialogClose();
            this.$message.success('发放空投成功!');
            this.getMyAirDrop();
          })
          .catch((err) => {
            this.$message.error(err.message);
          })
          .finally(() => {
            this.pubingWithCSV = false;
          });
      },
      /**
       * 发放空投对话框关闭回调
       */
      pubDialogClose() {
        this.pubDialogShow = false;
        this.pubAirDropId = '';
        this.csvFile = null;
        this.$refs['fileInput'].value = '';
        this.pubingWithCSV = false;
      },
      /**
       * 判断一个空投是不是Draft
       */
      isDraft(state) {
        return state === 'DRAFT';
      },
    },
  };
</script>

<style lang="less" scoped>
  .send-air-drop {
    padding: 5px 0;
    .top-panel {
      display: flex;
      justify-content: flex-end;
      margin: 5px 0;

      .el-button {
        width: 100%;
      }
    }
  }
</style>
