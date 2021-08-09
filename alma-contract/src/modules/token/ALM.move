address 0xb987f1ab0d7879b2ab421b98f96efb44 {
module ALM {
    use 0x1::Token;
    use 0x1::Account;

    const PRECISION: u8 = 9;

    struct ALM has copy, drop, store {}


    public fun init(account: &signer) {
        Token::register_token<ALM>(account, PRECISION);
        Account::do_accept_token<ALM>(account);
    }


    public fun mint(account: &signer, amount: u128) {
        let token = Token::mint<ALM>(account, amount);
        Account::deposit_to_self<ALM>(account, token)
    }


    public(script) fun init_script(account: signer) {
        Token::register_token<ALM>(&account, PRECISION);
        Account::do_accept_token<ALM>(&account);
    }

    public(script) fun mint_script(account: signer, amount: u128) {
        let token = Token::mint<ALM>(&account, amount);
        Account::deposit_to_self<ALM>(&account, token)
    }

    //    #[test(account = @0xb987f1ab0d7879b2ab421b98f96efb44)]
    //    fun test_token(account: signer) {
    //        use 0x1::Debug;
    //        //        use 0x1::Account;
    //        use 0x1::STC;
    //        use 0x1::Signer;
    //
    //        let account_address = Signer::address_of(&account);
    ////        Debug::print(&account_address);
    //
    //        Account::create_genesis_account(account_address);
    //        Account::do_accept_token<STC::STC>(&account);
    //
    //        let token_balance = Account::balance<STC::STC>(account_address);
    //
    //        Debug::print(&token_balance);
    ////        assert(token_balance == 0, 1);
    //
    //        let a = 1u8;
    ////        Debug::print(&a);
    //
    //        init(&account);
    //
    //        mint(&account, 10000 * 10000 * 2);
    //
    //        let alm_balance = Account::balance<ALM>(account_address);
    ////        Debug::print(&alm_balance);
    //        assert(alm_balance == 10000 * 10000 * 2, 2);
    //    }
}
}






