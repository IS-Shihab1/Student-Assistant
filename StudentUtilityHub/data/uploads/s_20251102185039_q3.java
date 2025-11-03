import java.util.*;

class bankaccount{
    private String accountNumber ;
    private String 	accountHolderName;
    private double balance; 
    private String	mobileNumber;

    static int totalaccount=0;
    static String bankname="GRAMMEN BANK";
    public bankaccount(String accountHolderName,String mobileNumber){
        this.accountHolderName=accountHolderName;
        if(mobileNumber.length()!=10){
            System.out.println("Invalid number.Number length must be 10");
        }else{
            this.mobileNumber=mobileNumber;
        }
        this.accountNumber="BANK"+mobileNumber.substring(6);
        totalaccount++;
    }
    public void deposit(double amount){
        if(amount>0){
            balance+=amount;
            System.out.println("Diposited :$"+amount);
        }else{
            System.out.println("Invalid diposite amount");
        }
    }
    public void withdraw(double amount){
        if(amount<=balance){
            balance-=amount;
            System.out.println("Withdrawn :$"+amount);
        }else{
            System.out.println("Invalid withdrawn amount");
        }
    }
    public void withdraw(double amount ,double fee){
        double total=amount+fee;
        if(total<=balance){
            balance-=total;
            System.out.println("withdrawn :$"+total);
        }else{
            System.out.println("Isufficient balanced");
        }
    }
    public void displayaccountinfo(){
        System.out.println("Account number:"+accountNumber);
        System.out.println("Holder :"+accountHolderName);
        System.out.println("Balance :"+balance);
    }
}
class q3 {
    public static void main(String[] args) {
        bankaccount account1=new bankaccount("Alice","9876543210");
        bankaccount account2=new bankaccount("Bob","1234567890");
        account1.deposit(2000);
        account2.deposit(2000);
        account1.withdraw(500);
        account2.withdraw(100,10);
        System.out.println();
        account1.displayaccountinfo();
        System.out.println();
        account2.displayaccountinfo();

        System.out.println("\nBank name :"+ bankaccount.bankname+", Total account :"+bankaccount.totalaccount);
    }
}
