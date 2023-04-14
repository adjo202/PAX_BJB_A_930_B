package com.pax.pay.menu;

import com.pax.pay.operator.ManagerChgPwdActivity;
import com.pax.pay.operator.OperAddActivity;
import com.pax.pay.operator.OperChgPwdActivity;
import com.pax.pay.operator.OperDelActivity;
import com.pax.pay.operator.OperQueryActivity;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

public class OperMenuActivity extends BaseMenuActivity {

    /**
     * 查询操作员， 添加操作员， 删除操作员， 修改操作员密码， 主管改密
     */
    public MenuPage createMenuPage() {

        MenuPage.Builder builder = new MenuPage.Builder(OperMenuActivity.this, 6, 3)
                .addMenuItem(getString(R.string.query_oper), R.drawable.query_operator, OperQueryActivity.class)
                .addMenuItem(getString(R.string.add_oper), R.drawable.add_operator, OperAddActivity.class)
                .addMenuItem(getString(R.string.delete_oper), R.drawable.del_operator, OperDelActivity.class)
                .addMenuItem(getString(R.string.modify_oper_pwd), R.drawable.modify_passwd, OperChgPwdActivity.class)
                .addMenuItem(getString(R.string.modify_manger_pwd), R.drawable.modify_mag_passwd,
                        ManagerChgPwdActivity.class);

        return builder.create();
    }

}
