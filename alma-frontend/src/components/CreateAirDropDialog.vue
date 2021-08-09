<template>
  <el-dialog
    class="cute-dialog"
    title="创建空投"
    top="5vh"
    :visible.sync="dialogVisible"
    @close="close"
  >
    <el-form>
      <el-form-item prop="token_id" label="Token: ">
        {{ airdropData.token_id }}
      </el-form-item>
      <el-form-item label="Project: ">
        <el-select v-model="airdropData.token" @change="tokenChanged" value-key="id">
          <el-option
            v-for="(project, index) in projectList"
            :key="index"
            :value="project"
            :label="getProjectTokenName(project.token_identifier)"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item prop="name" label="Project Name: ">
        <el-input v-model="airdropData.name"></el-input>
      </el-form-item>
      <el-form-item prop="owner_address" label="Owner Address: ">
        {{ airdropData.owner_address }}
      </el-form-item>
      <el-form-item prop="winners_count" label="Winners Count: ">
        <el-input
          v-model="airdropData.winners_count"
          maxlength="20"
          @input="interFilter('winners_count')"
        ></el-input>
      </el-form-item>
      <el-form-item prop="total_amount" label="Total Amount: ">
        <el-input
          v-model="airdropData.total_amount"
          maxlength="20"
          @input="interFilter('total_amount')"
        ></el-input>
      </el-form-item>
      <el-form-item prop="participate_url" label="Participate Url: ">
        <el-input v-model="airdropData.participate_url" maxlength="200"></el-input>
      </el-form-item>
      <!-- All Date -->
      <el-form-item prop="start_at" label="Start At: ">
        <el-date-picker value-format="timestamp" v-model="airdropData.start_at"></el-date-picker>
      </el-form-item>
      <el-form-item prop="participate_end_at" label="Participate End At: ">
        <el-date-picker
          value-format="timestamp"
          v-model="airdropData.participate_end_at"
        ></el-date-picker>
      </el-form-item>
      <el-form-item prop="airdrop_at" label="Airdrop At: ">
        <el-date-picker value-format="timestamp" v-model="airdropData.airdrop_at"></el-date-picker>
      </el-form-item>
      <el-form-item prop="end_at" label="End At: ">
        <el-date-picker value-format="timestamp" v-model="airdropData.end_at"></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="create">发放</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
  import { mapGetters } from 'vuex';

  export default {
    props: {
      visible: { type: Boolean, default: false },
    },
    data() {
      return {
        airdropData: {},
        projectList: [],
        dialogVisible: this.visible,
      };
    },
    components: {},
    watch: {
      visible() {
        if (this.visible) {
          this.getProjectList();
          this.$set(this.airdropData, 'owner_address', this.$accountHash);
        }
        this.dialogVisible = this.visible;
      },
      $accountHash() {
        this.$set(this.airdropData, 'owner_address', this.$accountHash);
      },
    },
    computed: {
      ...mapGetters(['$accountHash']),
    },
    methods: {
      /**
       * Split Token Name
       */
      getProjectTokenName(token_identifier) {
        return token_identifier.split('::').pop();
      },

      tokenChanged(val) {
        this.$set(this.airdropData, 'token_id', val.token_id);
        this.$set(this.airdropData, 'project_id', val.id);
      },
      /**
       * Get Project List
       */
      getProjectList() {
        this.$service.V1ProjectList().then((res) => {
          this.projectList = res;
        });
      },
      close() {
        this.$emit('update:visible', false);
        this.$set(this, 'airdropData', {});
      },
      interFilter(key) {
        this.$set(this.airdropData, key, this.airdropData[key].replace(/\D/g, ''));
      },
      /**
       * Create a AirDrop
       */
      create() {
        delete this.airdropData.token;
        this.$service.V1AirdropCreate(this.airdropData).then((res) => {
          this.$emit('done');
          this.close();
        });
      },
    },
  };
</script>

<style lang="less" scoped></style>
