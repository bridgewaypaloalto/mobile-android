package com.bcsv.mobile;

class ServingTurn {
    private String sundayDate;
    private String prayer;
    private String food;
    private String joycorner;
    private String prayDate;
    private String babysitter;

    public ServingTurn(String sundayDate, String prayer, String food, String joycorner,
                       String prayDate, String babysitter){
        this.babysitter = babysitter;
        this.food = food;
        this.joycorner = joycorner;
        this.prayer = prayer;
        this.prayDate = prayDate;
        this.sundayDate = sundayDate;
    }

    public String getSundayDate(){
        if(sundayDate.isEmpty())
            sundayDate = "Null";
        return sundayDate;
    }

    public String getPrayer(){
        if(prayer.isEmpty())
            prayer = "Null";
        return prayer;
    }

    public String getFood(){
        if(food.isEmpty())
            food = "Null";
        return food;
    }

    public String getJoycorner(){
        if(joycorner.isEmpty())
            joycorner = "Null";
        return joycorner;
    }

    public String getPrayDate(){
        if(prayDate.isEmpty())
            prayDate = "Null";
        return prayDate;
    }

    public String getBabysitter(){
        if(babysitter.isEmpty())
            babysitter = "Null";
        return babysitter;
    }
}