address 0xb987f1ab0d7879b2ab421b98f96efb44 {

module TestHelper {
    #[test_only]
    public fun init_test_account_(account: &signer): address {
        use 0x1::Signer;
        use 0x1::Account;
        use 0x1::STC;
        let account_address = Signer::address_of(account);
        Account::create_genesis_account(account_address);
        Account::do_accept_token<STC::STC>(account);
        return account_address
    }

    #[test_only]
    public fun check_balance<T : store>(account_address: address, target_amount: u128) {
        use 0x1::Account;

        let balance = Account::balance<T>(account_address);
        assert(balance == target_amount, 2);
    }

    #[test_only]
    fun test_str_eq() {
        let v = b"root1";
        let v2 = b"root1";
        //        Debug::print(&v);
        //        Debug::print(&v2);
        assert(v == v2, 3);
    }
}
}