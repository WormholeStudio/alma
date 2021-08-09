import TxnWrapper, { TXN_PARAMS_TYPE } from '@wormhole-stc/txn-wrapper';
import ChainMethod from 'config/ChainMethod';

/**
 *  获取公钥
 */
export const GetPublicKey = async (account) => {
  try {
    return await window.starcoin.request({
      method: 'eth_getEncryptionPublicKey',
      params: [account],
    });
  } catch (err) {
    console.log(err);
    Message.error(err.message);
  }
};

/**
 * 签名
 */
export const GetSign = async ({ account, nonce, message }) => {
  try {
    /**
     * 拿到用户对于一个nonce的签名
     */
    const sign = await window.starcoin.request({
      method: 'personal_sign',
      params: [`0x${Buffer.from('123456', 'utf-8').toString('hex')}`, account, nonce],
    });

    return sign;
  } catch (err) {
    console.log(err);
    Message.error(err.message);
  }
};

/**
 * 领取空投
 */
export const Claim_Airdrop = async ({
  token_identifier,
  owner_address,
  airdrop_id,
  root,
  proof,
}) => {
  return TxnWrapper({
    functionId: ChainMethod.CLAIM,
    returnType: token_identifier,
    params: [
      // owner_address this will return raw value
      owner_address,
      // airdrop_id: u64
      {
        value: airdrop_id,
        type: TXN_PARAMS_TYPE.U64,
      },
      // merkle_root: vector<u8>
      {
        value: root,
        type: TXN_PARAMS_TYPE['vector<u8>'],
      },
      // index: u64
      {
        value: proof.index,
        type: TXN_PARAMS_TYPE.U64,
      },
      // amount: u128
      {
        value: proof.amount,
        type: TXN_PARAMS_TYPE.U128,
      },
      // proof: vector<vector<u8>>
      {
        value: proof.proof,
        type: TXN_PARAMS_TYPE['vector<vector<u8>>'],
      },
    ],
  });
};

// https://github.com/starcoinorg/starcoin/blob/master/contrib-contracts/modules/MerkleDistributor.move
/**
 * 拿到csv文件转化之后的merkle tree数据,发放空投
 */
export const Pub_Token_Merkle = async ({
  airdrop_id,
  merkle_root,
  token_amounts,
  leafs,
  token_identifier,
}) => {
  return TxnWrapper({
    functionId: ChainMethod.CREATE,
    returnType: token_identifier,
    params: [
      {
        value: airdrop_id,
        type: TXN_PARAMS_TYPE.U64,
      },
      {
        value: merkle_root,
        type: TXN_PARAMS_TYPE['vector<u8>'],
      },
      {
        value: token_amounts,
        type: TXN_PARAMS_TYPE.U128,
      },
      {
        value: leafs,
        type: TXN_PARAMS_TYPE.U64,
      },
    ],
  });
};
