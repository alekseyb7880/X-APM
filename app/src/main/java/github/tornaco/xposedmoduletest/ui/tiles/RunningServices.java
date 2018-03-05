package github.tornaco.xposedmoduletest.ui.tiles;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.helper.RunningServicesActivity;

/**
 * Created by guohao4 on 2017/11/10.
 * Email: Tornaco@163.com
 */

public class RunningServices extends QuickTile {

    public RunningServices(final Context context) {
        super(context);
        this.titleRes = R.string.title_running_services;
        this.iconRes = R.drawable.ic_format_list_bulleted_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                context.startActivity(new Intent(context, RunningServicesActivity.class));
            }
        };
    }
}
