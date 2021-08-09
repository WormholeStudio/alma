//! account: alice, 10000 0x1::STC::STC
//! account: bob, 8000 0x1::STC::STC

//! sender: alice
//address bob = {{bob}};
script {
    //    use 0x1::Signer;
    //    use 0x1::STC;
    use 0xb987f1ab0d7879b2ab421b98f96efb44::MerkleDistributor;
    use 0xb987f1ab0d7879b2ab421b98f96efb44::ALM;
    //    use 0x1::Account;

    fun main(account: signer) {
        //        let account_address = Signer::address_of(&account);

        //        let token_balance = Account::balance<STC::STC>(account_address);
        ALM::init(&account);
        ALM::mint(&account, 10000 * 10000 * 2);
        let airdrop_id = 123456u64;
        let merkle_root = b"root:1";
        let leafs = 64u64;
        let token_amount = 666666u128;
        MerkleDistributor::create<ALM::ALM>(&account, copy airdrop_id, copy  merkle_root, copy token_amount, leafs);
    }
}
