package aurora_integration_tests.main;
import aurora_integration_tests.main.utils.Utils;
import org.hyperledger.indy.sdk.non_secrets.WalletRecord;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.util.HashMap;
import java.util.Map;

import static org.hyperledger.indy.sdk.wallet.Wallet.createWallet;

public class PrepareDatabaseRunnable implements Runnable{
    private String pool;
    private String walletType;
    private String config;
    private String creds;
    private int dbThreadsCnt;
    private int threadNum;
    private int totalWalletCnt;
    private int recordsPerWalletCnt;
    private String customTagsPerRecordData;
    private int percentOfCustomTagsPerRecord;

    public PrepareDatabaseRunnable(String pool, String walletType, String config, String creds, int dbThreadsCnt, int threadNum, int totalWalletCnt, int recordsPerWalletCnt, String customTagsPerRecordData, int percentOfCustomTagsPerRecord){
        this.pool = pool;
        this.walletType = walletType;
        this.config = config;
        this.creds = creds;
        this.dbThreadsCnt = dbThreadsCnt;
        this.threadNum = threadNum;
        this.totalWalletCnt = totalWalletCnt;
        this.recordsPerWalletCnt = recordsPerWalletCnt;
        this.customTagsPerRecordData = customTagsPerRecordData;
        this.percentOfCustomTagsPerRecord = percentOfCustomTagsPerRecord;
    }

    public void run() {
        for (int walletNum = (threadNum -1) * (totalWalletCnt/dbThreadsCnt)+1; walletNum<threadNum*(totalWalletCnt/dbThreadsCnt)+1; walletNum++){
            Wallet wallet = null;
            String walletName = "wallet_name_" + walletNum;
            String recordValue = Utils.generateRandomString(20);
            try {
                createWallet(pool, walletName, walletType, config, creds).get();
                wallet = Wallet.openWallet(walletName, null, creds).get();
                if (recordsPerWalletCnt != 0) {
                    for (int rec = 1; rec <= recordsPerWalletCnt; rec++){
                        String recordId = "record_id_" + walletNum + "_" + rec;
                        HashMap<String, String> tagsList = new HashMap<>();
                        HashMap<String, String> customTags;
                        if (customTagsPerRecordData != "" && percentOfCustomTagsPerRecord != 0) {
                            customTags = Utils.getHashMapFromJsonString(customTagsPerRecordData);
                            int numOfRecordsWithCustomTags = (recordsPerWalletCnt * percentOfCustomTagsPerRecord)/100;
                            for(Map.Entry<String, String> tag : customTags.entrySet()) {
                                String key = tag.getKey();
                                String value = tag.getValue();
                                if(rec>=1 && rec<=numOfRecordsWithCustomTags){
                                    tagsList.put(key, value);
                                } else {
                                    tagsList.put(key, Utils.generateRandomString(10));
                                }
                            }
                        }
                        String tags = Utils.getJsonStringFromHashMap(tagsList);
                        System.out.println("BEFORE ADD RECORD: " +rec);
                        WalletRecord.add(wallet, walletType, recordId, recordValue,tags).get();
                        System.out.println("AFTER ADD RECORD: " +rec);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
