address 0xb987f1ab0d7879b2ab421b98f96efb44 {

module MerkleDistributor {

    use 0x1::Vector;
    use 0x1::Signer;
    use 0x1::Token::{Token, Self};
    use 0x1::Collection2;
    use 0x1::Account;
    use 0xb987f1ab0d7879b2ab421b98f96efb44::MerkleProof;
//        use 0x1::Debug;
    use 0x1::Errors;
    use 0x1::Option::{Option, Self};

    struct Airdrop<T: store> has store {
        merkle_root: vector<u8>,
        airdrop_id: u64,
        tokens: Token<T>,
        claimed_bitmap: vector<u128>,

    }


    const INVALID_PROOF: u64 = 1;
    const ALREADY_CLAIMED: u64 = 2;
    const INSUFFICIENT_AVAILABLE_BALANCE: u64 = 3;
    const AIRDROP_NOT_EXISTS: u64 = 4;

    public fun cancel_all_airdrop<T : store>(account: &signer) {
        let owner_address = Signer::address_of(account);
        let c = Collection2::exists_at<Airdrop<T>>(owner_address);
        assert(c, 185);
        let c = Collection2::borrow_collection<Airdrop<T>>(account, owner_address);
        let c_len = Collection2::length<Airdrop<T>>(&c);
        let i = 0;
        while (i < c_len) {
            let airdrop = Collection2::remove<Airdrop<T>>(&mut c, 0);
            let amount = Token::value<T>(&airdrop.tokens);
            let unclaimed_tokens = Token::withdraw(&mut airdrop.tokens, amount);
            if (amount > 0) {
                Account::deposit_to_self<T>(account, unclaimed_tokens);
            }else {
                Token::destroy_zero<T>(unclaimed_tokens);
            };
            let Airdrop<T> {
                merkle_root: _,
                airdrop_id: _,
                tokens: coins,
                claimed_bitmap: _,
            } = airdrop;
            Token::destroy_zero<T>(coins);
            i = i + 1;
        };
        Collection2::return_collection(c);
        Collection2::destroy_collection<Airdrop<T>>(account);
    }

    fun build_airdrop<T: store>(airdrop_id: u64, merkle_root: vector<u8>, tokens: Token::Token<T>, leafs: u64): Airdrop<T> {
        let bitmap_count = leafs / 128;
        if (bitmap_count * 128 < leafs) {
            bitmap_count = bitmap_count + 1;
        };
        let claimed_bitmap = Vector::empty();
        let j = 0;
        while (j < bitmap_count) {
            Vector::push_back(&mut claimed_bitmap, 0u128);
            j = j + 1;
        };
        return Airdrop<T> {
            merkle_root,
            airdrop_id,
            tokens,
            claimed_bitmap
        }
    }


    public fun create<T: store>(account: &signer, airdrop_id: u64, merkle_root: vector<u8>, token_amount: u128, leafs: u64) {
        let owner_address = Signer::address_of(account);
        let token_balance = Account::balance<T>(owner_address);
        assert(token_balance > token_amount, Errors::custom(INSUFFICIENT_AVAILABLE_BALANCE));
        let tokens = Account::withdraw<T>(account, token_amount);
        let airdrop = build_airdrop(airdrop_id, merkle_root, tokens, leafs);
        if (!Collection2::exists_at<Airdrop<T>>(owner_address)) {
            Collection2::create_collection<Airdrop<T>>(account, false, true);
        };
        Collection2::put(account, Signer::address_of(account), airdrop);
    }


    public fun is_claimd<T: store>(account: &signer, owner_address: address, airdrop_id: u64, merkle_root: vector<u8>, index: u64): bool {
        let idx_option = get_airdrop_idx<T>(account, &owner_address, &airdrop_id, &merkle_root);
        assert(!Option::is_none(&idx_option), Errors::custom(AIRDROP_NOT_EXISTS));
        let c = Collection2::borrow_collection<Airdrop<T>>(account, owner_address);
        let idx = Option::borrow<u64>(&idx_option);
        let airdrop = Collection2::borrow_mut<Airdrop<T>>(&mut c, *idx);
        let rst = is_claimed_<T>(airdrop, index);
        Collection2::return_collection(c);
        return rst
    }

    public fun claim<T: store>(account: &signer, owner_address: address, airdrop_id: u64,
                               merkle_root: vector<u8>, index: u64, amount: u128, proof: vector<vector<u8>>) {
        let idx_option = get_airdrop_idx<T>(account, &owner_address, &airdrop_id, &merkle_root);
        assert(!Option::is_none(&idx_option), Errors::custom(AIRDROP_NOT_EXISTS));
        let c = Collection2::borrow_collection<Airdrop<T>>(account, owner_address);
        let idx = Option::borrow<u64>(&idx_option);
        let airdrop = Collection2::borrow_mut<Airdrop<T>>(&mut c, *idx);
        let claimed = is_claimed_<T>(airdrop, index);
        assert(!claimed, Errors::custom(ALREADY_CLAIMED));

        //verify proof
        let account_address = Signer::address_of(account);
        let leaf_hash = MerkleProof::hash_leaf(&index, &airdrop_id, &owner_address, &amount);
        let rst = MerkleProof::verify(&proof, &merkle_root, leaf_hash, &index);
        assert(!rst, Errors::custom(INVALID_PROOF));

        let is_accept_token = Account::is_accepts_token<T>(account_address);
        if (!is_accept_token) {
            Account::do_accept_token<T>(account);
        };
        let claimed_tokens = Token::withdraw(&mut airdrop.tokens, amount);
        Account::deposit(Signer::address_of(account), claimed_tokens);
        set_claimed(airdrop, index);
        Collection2::return_collection(c);
    }

    fun set_claimed<T: store>(airdrop: &mut Airdrop<T>, index: u64) {
        let claimed_word_index = index / 128;
        let claimed_bit_index = ((index % 128) as u8);
        let word = Vector::borrow_mut(&mut airdrop.claimed_bitmap, claimed_word_index);
        let mask = 1u128 << claimed_bit_index;
        *word = (*word | mask);
    }

    fun is_claimed_<T: store>(airdrop: &Airdrop<T>, index: u64): bool {
        let claimed_word_index = index / 128;
        let claimed_bit_index = ((index % 128) as u8);
        let word = Vector::borrow(&airdrop.claimed_bitmap, claimed_word_index);
        let mask = 1u128 << claimed_bit_index;
        (*word & mask) == mask
    }

    fun get_airdrop_idx<T: store>(account: &signer, owner_address: &address, airdrop_id: &u64, merkle_root: &vector<u8>): Option<u64> {
        let idx = Option::none<u64>();
        if (Collection2::exists_at<Airdrop<T>>(*owner_address)) {
            let c = Collection2::borrow_collection<Airdrop<T>>(account, *owner_address);
            let c_len = Collection2::length<Airdrop<T>>(&c);
            let i = 0;

            while (i < c_len) {
                let airdrop = Collection2::borrow<Airdrop<T>>(&mut c, i);
                if (airdrop.airdrop_id == *airdrop_id && *&airdrop.merkle_root == *merkle_root) {
                    Option::fill(&mut idx, i);
                    break
                };
                i = i + 1;
            };
            Collection2::return_collection(c);
        };
        return idx
    }




    #[test(account = @0x0000000000000000000000000a550c18, account2 = @0xffebfbb40556a9f585958bcb3fc233b3, alm_account = @0xb987f1ab0d7879b2ab421b98f96efb44)]
    fun test_cancel_all_airdrop(account: signer, account2: signer, alm_account: signer) {
        use 0x1::Signer;
        use 0xb987f1ab0d7879b2ab421b98f96efb44::TestHelper;
        use 0xb987f1ab0d7879b2ab421b98f96efb44::MerkleDistributor;
        use 0xb987f1ab0d7879b2ab421b98f96efb44::ALM;
//        use 0x1::Debug;
        use 0x1::Collection2;
        let account_address = Signer::address_of(&account);
        let _account_address2 = TestHelper::init_test_account_(&account2);
        test_create(&account, alm_account);


        let airdrop_id = 1u64;
        let merkle_root = b"3071293f3d19e1f4b60921c5bce50c2de49ba1c2ec9cbedb2cecbcee44068401";
        let leafs = 4u64;
        let token_amount = 10u128;
        MerkleDistributor::create<ALM::ALM>(&account, copy airdrop_id, copy  merkle_root, copy token_amount, leafs);

        //        TestHelper::check_balance<ALM::ALM>(account_address, 1000000000 * 20 - token_amount);

        let c = Collection2::borrow_collection<Airdrop<ALM::ALM>>(&account, account_address);
        let c_len = Collection2::length<Airdrop<ALM::ALM>>(&c);
        assert(c_len == 2, 180);
        Collection2::return_collection(c);
        MerkleDistributor::cancel_all_airdrop<ALM::ALM>(&account);
        let c = Collection2::exists_at<Airdrop<ALM::ALM>>(account_address);
        assert(!c, 185);
        TestHelper::check_balance<ALM::ALM>(account_address, 1000000000 * 20);
    }

    #[test(account = @0x0000000000000000000000000a550c18, account2 = @0xffebfbb40556a9f585958bcb3fc233b3, alm_account = @0xb987f1ab0d7879b2ab421b98f96efb44)]
    fun test_claim(account: signer, account2: signer, alm_account: signer) {
        use 0x1::Signer;
        use 0xb987f1ab0d7879b2ab421b98f96efb44::TestHelper;
        use 0xb987f1ab0d7879b2ab421b98f96efb44::MerkleDistributor;
        use 0xb987f1ab0d7879b2ab421b98f96efb44::ALM;
        //        use 0x1::Debug;
        let account_address = Signer::address_of(&account);
        let account_address2 = TestHelper::init_test_account_(&account2);
        let (airdrop_id, merkle_root, _leafs) = test_create(&account, alm_account);

        let index = 1u64;
        let amount = 1000000000u128;
        let proof_1 = x"190974213ad12de7b80a623097d74a88aee345336dfae5f7bc887440c8a514c0";
        let proof_2 = x"931e4911be9c65700da29d24ac29f19b41eb95f37370aa6897d3c061a1d6b718";
        let proof = Vector::empty();
        Vector::push_back(&mut proof, proof_1);
        Vector::push_back(&mut proof, proof_2);
        let is_claimed = MerkleDistributor::is_claimd<ALM::ALM>(&account2, account_address, copy airdrop_id, copy  merkle_root, index);
        assert(!is_claimed, 1001);
        MerkleDistributor::claim<ALM::ALM>(&account2, account_address, copy airdrop_id, copy  merkle_root, index, amount, copy proof);
        TestHelper::check_balance<ALM::ALM>(account_address2, amount);
        is_claimed = MerkleDistributor::is_claimd<ALM::ALM>(&account2, account_address, copy airdrop_id, copy  merkle_root, index);
        assert(is_claimed, 1002);
    }


    #[test(account = @0x0000000000000000000000000a550c18, alm_account =@0xb987f1ab0d7879b2ab421b98f96efb44)]
    fun test_create(account: &signer, alm_account: signer): (u64, vector<u8>, u64) {
        use 0xb987f1ab0d7879b2ab421b98f96efb44::MerkleDistributor;
        use 0xb987f1ab0d7879b2ab421b98f96efb44::ALM;
        use 0xb987f1ab0d7879b2ab421b98f96efb44::TestHelper;
        use 0x1::Account;
        let account_address = TestHelper::init_test_account_(account);
        TestHelper::init_test_account_(&alm_account);
        ALM::init(&alm_account);
        ALM::mint(&alm_account, 1000000000 * 20);
        let tokens = Account::withdraw<ALM::ALM>(&alm_account, 1000000000 * 20);

        Account::do_accept_token<ALM::ALM>(account);
        Account::deposit(account_address, tokens);
        TestHelper::check_balance<ALM::ALM>(account_address, 1000000000 * 20);
        let airdrop_id = 92386324538284u64;
        let merkle_root = b"4071293f3d19e1f4b60921c5bce50c2de49ba1c2ec9cbedb2cecbcee44068401";
        let leafs = 3u64;
        let token_amount = 1000000000u128 + 1000000000u128 + 2000000000u128;


        MerkleDistributor::create<ALM::ALM>(account, copy airdrop_id, copy  merkle_root, copy token_amount, leafs);
        TestHelper::check_balance<ALM::ALM>(account_address, 1000000000 * 20 - token_amount);
        (airdrop_id, merkle_root, leafs)
    }
}
}