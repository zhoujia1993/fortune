package yunbi;

import yunbi.model.AccountInfo;

/**
 * Created by zhoujia on 2017/6/16.
 */
public class AccountCenter {

    private AccountInfo accountInfo;

    private AccountCenter() {

    }

    public static AccountCenter getInstance() {
        return AccountInner.instance;
    }

    private static class AccountInner {
        public static final AccountCenter instance = new AccountCenter();
    }

    public void setAccount(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public AccountInfo getAccount() {
        return accountInfo;
    }
}
