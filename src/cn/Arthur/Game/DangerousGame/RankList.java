package cn.Arthur.Game.DangerousGame;

import cn.bmob.v3.BmobObject;

public class RankList extends BmobObject {
    private String NAME;
    private String IMEI;
    private int SCORE;

    public String getNAME() {
        return NAME;
    }
    public void setNAME(String NAME) {
        this.NAME = NAME;
    }
    
    public String getIMEI() {
        return IMEI;
    }
    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }
    
    public void setSCORE(int SCORE){
    	this.SCORE=SCORE;
    }
    public int getSCORE(){
    	return SCORE;
    }
    
}