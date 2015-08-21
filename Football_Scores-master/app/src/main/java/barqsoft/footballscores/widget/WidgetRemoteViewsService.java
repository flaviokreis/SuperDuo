package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by flaviokreis on 20/08/15.
 */
public class WidgetRemoteViewsService extends RemoteViewsService {
    public static final int COL_HOME        = 3;
    public static final int COL_AWAY        = 4;
    public static final int COL_HOME_GOALS  = 6;
    public static final int COL_AWAY_GOALS  = 7;
    public static final int COL_DATE        = 1;
    public static final int COL_LEAGUE      = 5;
    public static final int COL_MATCHDAY    = 9;
    public static final int COL_ID          = 8;
    public static final int COL_MATCHTIME   = 2;
    public double detail_match_id           = 0;

    SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() { /* no code */ }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(DatabaseContract.BASE_CONTENT_URI,
                        null, null, null, null );
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);

                String homeName = data.getString(COL_HOME);
                String awayName = data.getString(COL_AWAY);
                String match    = data.getString(COL_MATCHTIME);
                String scores   = Utilies.getScores(data.getInt(COL_AWAY_GOALS), data.getInt(COL_HOME_GOALS));

                String dateString = data.getString(COL_DATE);

                String dateTime = match;

                if(!TextUtils.isEmpty(dateString)){
                    try {
                        Date date = mformat.parse(dateString);
                        if(date != null){
                            String dateValue = Utilies.getDayName(getApplicationContext(), date.getTime());
                            dateTime = dateValue + "\n" + match;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        dateTime = dateString + "\n" + match;
                    }
                }

                views.setTextViewText(R.id.home_name, homeName);
                views.setTextViewText(R.id.away_name, awayName);
                views.setTextViewText(R.id.data_textview, dateTime);
                views.setTextViewText(R.id.score_textview, scores);
                views.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(
                        data.getString(COL_HOME)));
                views.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(
                        data.getString(COL_AWAY)));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    String description;
                    if((data.getInt(COL_HOME_GOALS) < 0) || (data.getInt(COL_AWAY_GOALS) < 0)){
                        description = homeName + " versus " +  awayName + ", " + dateTime;
                    }
                    else{
                        description = homeName + " " + data.getInt(COL_HOME_GOALS)
                                + ", " +  awayName + " " + data.getInt(COL_AWAY_GOALS)
                                + ". " + dateTime;
                    }

                    setRemoteContentDescription(views, description);
                }

                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_list_item, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(COL_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
