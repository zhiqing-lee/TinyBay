package info.zhiqing.tinypiratebay.spider;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import info.zhiqing.tinypiratebay.entities.Torrent;
import info.zhiqing.tinypiratebay.entities.TorrentDetail;
import info.zhiqing.tinypiratebay.util.CategoryUtil;
import info.zhiqing.tinypiratebay.util.ConfigUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by zhiqing on 18-1-6.
 */

public class SpiderClient {
    public static final String TAG = "SpiderClient";

    private Spider spider;

    private static SpiderClient instance;

    public static SpiderClient getInstance() {
        if (instance == null) {
            instance = new SpiderClient(ConfigUtil.BASE_URL);
        }
        return instance;
    }

    public static void buildNewInstance() {
        instance = new SpiderClient(ConfigUtil.BASE_URL);
    }

    private SpiderClient(String url) {
        spider = new SpiderLocal(url);
    }

    public Observable<List<Torrent>> fetchTorrentsByUrl(final String url) {
        return Observable.create(new ObservableOnSubscribe<List<Torrent>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Torrent>> e) throws Exception {
                List<Torrent> list = spider.list(url);
                Log.d(TAG, "Get " + url + " : " + list.size());
                if (!ConfigUtil.PORN_MODE) {
                    List<Torrent> newList = new ArrayList<>();
                    for (Torrent item : list) {
                        if (!CategoryUtil.parentCode(item.getTypeCode()).equals("500")) {
                            newList.add(item);
                        }
                    }
                    e.onNext(newList);
                    e.onComplete();
                }
                e.onNext(list);
                e.onComplete();
            }
        });
    }

    public Observable<TorrentDetail> fetchTorrentDetail(final String code) {
        return Observable.create(new ObservableOnSubscribe<TorrentDetail>() {
            @Override
            public void subscribe(ObservableEmitter<TorrentDetail> e) throws Exception {
                TorrentDetail detail = spider.detail(code);
                e.onNext(detail);
                e.onComplete();
            }
        });
    }
}
