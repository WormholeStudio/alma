address 0xb987f1ab0d7879b2ab421b98f96efb44 {
module MerkleProof {
    use 0x1::Hash;
    use 0x1::BCS;
    use 0x1::Vector;
    use 0x1::Compare;

    public fun verify(proof: &vector<vector<u8>>, root: &vector<u8>, leaf: vector<u8>, _index: &u64): bool {
        let computed_hash = leaf;
        let i = 0;
        let proof_length = Vector::length(proof);

        while (i < proof_length) {
            let sibling = Vector::borrow(proof, i);
            if (Compare::cmp_bytes(&computed_hash,sibling) < 2) {
                let concated = concat(computed_hash, *sibling);
                computed_hash = Hash::sha3_256(concated);
            } else {
                let concated = concat(*sibling, computed_hash);
                computed_hash = Hash::sha3_256(concated);
            };
            i = i + 1;
        };
        &computed_hash == root
    }


    fun concat(v1: vector<u8>, v2: vector<u8>): vector<u8> {
        Vector::append(&mut v1, v2);
        v1
    }

    public fun hash_leaf(index: &u64, airdrop_id: &u64, account: &address, amount: &u128): vector<u8> {
        let leaf = Vector::empty();
        Vector::append(&mut leaf, BCS::to_bytes(index));
        Vector::append(&mut leaf, BCS::to_bytes(airdrop_id));
        Vector::append(&mut leaf, BCS::to_bytes(account));
        Vector::append(&mut leaf, BCS::to_bytes(amount));
        Hash::sha3_256(leaf)
    }




    #[test]
    fun test_verify_left() {
        //        use 0x1::Debug;
        use 0x1::Vector;
        use 0xb987f1ab0d7879b2ab421b98f96efb44::MerkleProof;
        let address = @0xffebfbb40556a9f585958bcb3fc233b8;
        let index = 6u64;
        let amount = 1000000000u128;
        let airdrop_id = 92386324538284u64;
        let root =x"21126fe4d1cac6d5beef09702369d492032bcc091cbdd228f8c60b4cfc7daf4c";


        let proof_1 =   x"3b929f171f0dea56a4fdc35d92d0cefce5b7ac4e412c961582984875df11d940";
        let proof_2 =   x"224c355b8582dbd5aeebeefbd43acb5267caa28d9a56e97c4defb2d073812bb1";
        let proof_3 =   x"e7057ebf0274d2c5f847655355c6dd04e4badaae6b75bc0cb650f15e6cdc7112";
        let proof_4 =   x"e908b65f58ee19fa8b6e387f173bfbcda113bc2bb01efaaf5d1d8a944676c3a2";
        let proof = Vector::empty();
        Vector::push_back( &mut proof, proof_1);
        Vector::push_back( &mut proof, proof_2);
        Vector::push_back( &mut proof, proof_3);
        Vector::push_back( &mut proof, proof_4);

        let leaf_hash = MerkleProof::hash_leaf( & index, & airdrop_id, & address, & amount);
        let rst = MerkleProof::verify( & proof, & root, leaf_hash, & index);
        assert(rst, 108);
    }

    #[test]
    fun test_verify_right() {
        //        use 0x1::Debug;
        use 0x1::Vector;
        use 0xb987f1ab0d7879b2ab421b98f96efb44::MerkleProof;
        let address = @0xffebfbb40556a9f585958bcb3fc233b3;
        let index = 1u64;
        let amount = 1000000000u128;
        let airdrop_id = 92386324538284u64;
        let root =x"e7057ebf0274d2c5f847655355c6dd04e4badaae6b75bc0cb650f15e6cdc7112";

        let proof_1 = x"190974213ad12de7b80a623097d74a88aee345336dfae5f7bc887440c8a514c0";
        let proof_2 = x"9a38fb89f44526d18ed647c362826f86109710dcea40fa4419d36c04788137c8";

        let proof = Vector::empty();
        Vector::push_back( &mut proof, proof_1);
        Vector::push_back( &mut proof, proof_2);

        let leaf_hash = MerkleProof::hash_leaf( & index, & airdrop_id, & address, & amount);
        let rst = MerkleProof::verify( & proof, & root, leaf_hash, & index);
        assert(rst, 108);
    }


    #[test]
    fun test_verify_alone_leaf() {
        //        use 0x1::Debug;
        use 0x1::Vector;
        use 0xb987f1ab0d7879b2ab421b98f96efb44::MerkleProof;
        let address = @0xffebfbb40556a9f585958bcb3fc23311;
        let index = 9u64;
        let amount = 1000000000u128;
        let airdrop_id = 92386324538284u64;
        let root =x"21126fe4d1cac6d5beef09702369d492032bcc091cbdd228f8c60b4cfc7daf4c";


        let proof_1 =   x"a46a243a18f89ef37902bd66a49840e85e137380df9717af686350cee8a104cd";
        let proof_2 =   x"b894807aa65beddeeca32e3413c6c7b49a6d784f5a69cacb2d262dc88a4182e7";
        let proof = Vector::empty();
        Vector::push_back( &mut proof, proof_1);
        Vector::push_back( &mut proof, proof_2);

        let leaf_hash = MerkleProof::hash_leaf( & index, & airdrop_id, & address, & amount);
        let rst = MerkleProof::verify( & proof, & root, leaf_hash, & index);
        assert(rst, 108);
    }
}
}
