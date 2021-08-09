address 0xb987f1ab0d7879b2ab421b98f96efb44 {

module MerkleDistributorScriptV2 {
    use 0xb987f1ab0d7879b2ab421b98f96efb44::MerkleDistributor;

    public(script) fun create<T: store>(account: signer, airdrop_id: u64, merkle_root: vector<u8>, token_amount: u128, leafs: u64) {
        MerkleDistributor::create<T>(&account, airdrop_id, merkle_root, token_amount, leafs)
    }


    public(script) fun claim_script<T: store>(account: signer, owner_address: address, airdrop_id: u64,
                                              merkle_root: vector<u8>, index: u64, amount: u128, proof: vector<vector<u8>>
    ) {
        MerkleDistributor::claim<T>(&account, owner_address, airdrop_id, merkle_root, index, amount, proof);
    }

    public(script) fun cancel_all_airdrop<T: store>(account: signer) {
        MerkleDistributor::cancel_all_airdrop<T>(&account);
    }
}
}
