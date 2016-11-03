package Widgets;


import java.awt.Frame;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JOptionPane;


public class parseEquation {
	private static Stack<Character> theStack;
	private static char charKey=(char)65; //A-Z  97 a
	private  static Map<String,Character>  hashMap=new HashMap<String,Character>();
	private static String result;
	private static int orignalSize;
	private String negativePart;
	private String positivePart;

	
	public parseEquation(String rxn) {
		//String rxn = "(vcb*kcom4*Cb*(Cdk1tot-(Mbi+Mb+Mbp27))+kdecom4*Mbi-Vdb*Cb/(Kdb+Cb)*(Cdc20a/(Kdbcdc20+Cdc20a)+Cdh1a/(Kdbcdh1+Cdh1a))-kddb*Cb)*eps";
		//String internalStr1 = "(343-2*233*3+34*3444-3*(1/(34+90)))*3";
		if( rxn.substring(0, 1).equals("-") )
    		rxn = "0"+rxn;
		
		String output  = processBeforeEvaluation(rxn);
		//System.out.println("the internal expression is :"+output);
		String postfixStr= translateToPostfixExpression(output);
		//System.out.println("the postfix expression is :"+postfixStr);

		String origSeq = postfixStr;
		int flag = 0;
		//(...)*a or (...)/a         
		if( postfixStr.substring(postfixStr.length()-1).equals("*") && postfixStr.substring(postfixStr.length()-2,postfixStr.length()-1).toCharArray()[0]>=65 && postfixStr.substring(postfixStr.length()-2,postfixStr.length()-1).toCharArray()[0]!=94){
			origSeq = postfixStr.substring(0, postfixStr.length()-2);
			flag= 1;
		}else if( postfixStr.substring(postfixStr.length()-1).equals("/") && postfixStr.substring(postfixStr.length()-2,postfixStr.length()-1).toCharArray()[0]>=65 && postfixStr.substring(postfixStr.length()-2,postfixStr.length()-1).toCharArray()[0]!=94 ){
			origSeq = postfixStr.substring(0, postfixStr.length()-2);
			flag=1;
		}

		//a*(...) or a/(...)
		if( output.substring(1,2).equals("*") && output.substring(2, 3).equals("(") && output.substring(output.length()-1).equals(")") && ifBracketEqual(output.substring(3)) && output.substring(0,1).toCharArray()[0]>=65 ){
			//check if a*(...) braket match
			Stack<Integer> left = new Stack<Integer>(); 
			int i = 3;
			for(;i<output.length();i++){
				if(output.substring(i, i+1).equals("(")) left.push(i);
				else if(output.substring(i, i+1).equals(")")) 
					if( left.size()!=0 ) left.pop();
					else break;			
			}
			if( i== output.length()-1 ){
				origSeq = postfixStr.substring(1, postfixStr.length()-1);
				flag=2;
			}
			
				
		}else if( output.substring(1,2).equals("/") && output.substring(2, 3).equals("(") && output.substring(output.length()-1).equals(")") && ifBracketEqual(output.substring(3)) && output.substring(0,1).toCharArray()[0]>=65 ){
			//check if a*(...) braket match
			Stack<Integer> left = new Stack<Integer>(); 
			int i = 3;
			for(;i<output.length();i++){
				if(output.substring(i, i+1).equals("(")) left.push(i);
				else if(output.substring(i, i+1).equals(")")) 
					if( left.size()!=0 ) left.pop();
					else break;			
			}
			if( i== output.length()-1 ){
				origSeq = postfixStr.substring(1, postfixStr.length()-1);
				flag=2;
			}
	
		}

		String result=evaluatePostfixExpression(origSeq,0);

		//update hashmap
		for(int i=orignalSize+1;i<charKey;i++){
			Iterator iter = hashMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				char val = (Character) entry.getValue();

				if( val==i ){
					String part1 = valueGetKey(hashMap, key.substring(1, 2).toCharArray()[0]);
					String part2 = valueGetKey(hashMap, key.substring(2, 3).toCharArray()[0]);
					hashMap.remove(key);
					key = "("+part1+key.substring(3, 4)+part2+")";
					hashMap.put(key, val);
					break;
				}
			}
		}



		negativePart = "";
		positivePart = valueGetKey(hashMap, result.substring(0, 1).toCharArray()[0])+"+";
		for(int i=0;i<result.length();i++)
			if( result.substring(i,i+1).equals("-") )
				negativePart += valueGetKey(hashMap, result.substring(i-1, i).toCharArray()[0])+"+";
			else if( result.substring(i,i+1).equals("+") )
				positivePart += valueGetKey(hashMap, result.substring(i-1, i).toCharArray()[0])+"+";


		if( flag==1 ){
			negativePart = "("+negativePart.substring(0, negativePart.length()-1)+")"+postfixStr.substring(postfixStr.length()-1,postfixStr.length())+valueGetKey(hashMap, postfixStr.substring(postfixStr.length()-2,postfixStr.length()-1).toCharArray()[0]);
			positivePart = "("+positivePart.substring(0, positivePart.length()-1)+")"+postfixStr.substring(postfixStr.length()-1,postfixStr.length())+valueGetKey(hashMap, postfixStr.substring(postfixStr.length()-2,postfixStr.length()-1).toCharArray()[0]);
		}else if( flag==2){
			negativePart = "("+negativePart.substring(0, negativePart.length()-1)+")"+postfixStr.substring(postfixStr.length()-1,postfixStr.length())+valueGetKey(hashMap, postfixStr.substring(0,1).toCharArray()[0]);
			positivePart = "("+positivePart.substring(0, positivePart.length()-1)+")"+postfixStr.substring(postfixStr.length()-1,postfixStr.length())+valueGetKey(hashMap, postfixStr.substring(0,1).toCharArray()[0]);  
		}else{
			negativePart = negativePart.substring(0, negativePart.length()-1);
			positivePart = positivePart.substring(0, positivePart.length()-1);
		}


		//System.out.println("the final result is :"+negativePart+"\n"+positivePart);
	}

	private static boolean ifBracketEqual(String origSeq) {
		boolean result = true;
		String seq = origSeq;

		int count1 = 0;
		int index = seq.indexOf("("); 
		while (index != -1) {
			index = seq.indexOf("("); 
			if(index!=-1) seq = seq.substring(index + 1); 
			count1++;
		}

		int count2 = 0;
		seq= origSeq;
		index = seq.indexOf("("); 
		while (index != -1) {
			index = seq.indexOf("("); 
			if(index!=-1) seq = seq.substring(index + 1); 
			count2++;
		}

		result = (count1==count2);
		return result;
	}

	private static String processBeforeEvaluation(String input) {
		String[] items = input.split("\\*|\\^|\\+|\\/|-|\\(|\\)");
		for(int i=0;i<items.length;i++) {
			if( !items[i].equals("") ){
				String temp=items[i];
				if( !hashMap.containsKey(temp) ){
					hashMap.put(temp,charKey);
					charKey=(char)(charKey+1);
					if( charKey == 94 )
						charKey=(char)(charKey+1);
				}
			}
		}

		if( charKey == 94 )
			charKey=(char)(charKey+1);

		String output = "";  
		for(int i=0;i<input.length();i++){
			String temp = input.substring(i, i+1);
			if( temp.matches("\\*|\\^|\\+|\\/|-|\\(|\\)") )
				output += temp;
			else{
				String t = temp;
				i++;
				if( i==input.length() ){
        			output += hashMap.get(t);
        			i--;
        		}else{
        			temp = input.substring(i, i+1);
        			while( !temp.matches("\\*|\\^|\\+|\\/|-|\\(|\\)|$") ){
        				t += temp;
        				i++;
        				if( i!=input.length() )
        					temp = input.substring(i, i+1);
        				else
        					break;
        			}
        			output += hashMap.get(t);
        			i--;
        		}
			}
		}

		orignalSize = charKey-1;
		return output;  
	}


	private static String translateToPostfixExpression(String input) {
		theStack=new Stack<Character>();
		result="";
		for(int j=0; j<input.length(); j++) {
			char ch=input.charAt(j);
			switch(ch) {
			case '+':
			case '-':
			case '*':
			case '/':
			case '^':
				gotOper(ch);
				break;
			case '(':
				theStack.push(ch);
				break;
			case ')':
				gotParen(ch);
				break;
			default :
				result+=ch;
				break;
			}  //end switch
		} //end for loop
		while(!theStack.empty()) {
			result+=theStack.pop();
		}
		return result;
	}

	private static void gotOper(char opThis) {
		while(!theStack.empty()) {
			char opTop=theStack.pop();
			if(opTop=='(') {
				theStack.push(opTop);
				break;
			} else {
				if(opThis=='*'||opThis=='/') {   //如果是本次是乘除,栈里最多可弹出一个乘号
					if(opTop=='+'||opTop=='-') {
						theStack.push(opTop);
					} else {
						result+=opTop;
					}
					break;
				}else if(opThis=='^'){
					if(opTop=='+'||opTop=='-'||opTop=='*'||opTop=='/') {
						theStack.push(opTop);
					} else {   //如果是本次是乘除,栈里最多可弹出一个乘号
						result+=opTop;
					}
					break;
				}else {            //如果是本次是加减,栈里最多可弹出一次乘除,再加一次加减
					result+=opTop;
				}
			} //end else
		}//end while
		theStack.push(opThis);
	}//end gotOper()



	private static void gotParen(char ch) {
		while(!theStack.empty()) {
			char chx=theStack.pop();
			if(chx=='(')
				break;
			else
				result+=chx;
		} //end while
	}


	private static String evaluatePostfixExpression(String input, int j2) {   	
		char ch;
		String interAns = "";
		String str1,str2;
		theStack=new Stack<Character>(); //重置栈,后缀表达式求值是将数字放入栈中
		try {        	
			for(int j=j2; j<input.length()-1; j++) {
				String tt = input.replaceAll("\\\\","\\\\\\\\");  
				String[] a = tt.split("\\*|\\+|\\^|\\/|-|\\(|\\)");
				ArrayList<String> b= new ArrayList<String>();
				for(int i=0;i<a.length;i++)
					if(!a[i].equals(""))
						b.add(a[i]);

				tt = tt.replace("\\\\", "\\");
				if( b.size()*2+1 != tt.split("").length ){
					ch=input.charAt(j);
					if(ch>=65 && ch!=94) {  //j代表一个double型的数字
						theStack.push(ch);
					} else {

						switch(ch) {
						case '+':
							char ch1 = input.charAt(j+1);
							char ch4 = input.charAt(j-1);
							char ch5 = input.charAt(j-2);
							if( String.valueOf(ch1).equals("*") || String.valueOf(ch1).equals("/") ){
								if( ch4>=65 && ch5>=65 && ch4!=94 && ch5!=94 ){
									str2 = String.valueOf(theStack.pop()); //valueGetKey(hashMap, theStack.pop());
									str1= String.valueOf(theStack.pop()); //valueGetKey(hashMap, theStack.pop());
									interAns=str1+str2+"+";
								}else{ //+-*
									String ttt = getEquaFromBegin(input.substring(0, j+2));
									j++;
									input = ttt+input.substring(j+1);
								}
							}else if( !String.valueOf(ch4).matches("\\*|\\^|\\+|\\/|-|\\(|\\)") && !String.valueOf(ch5).matches("\\*|\\^|\\+|\\/|-|\\(|\\)") ){
								if( ch4>=65 && ch5>=65 && ch4!=94 && ch5!=94 ){
									str2 = String.valueOf(theStack.pop()); //valueGetKey(hashMap, theStack.pop());
									str1= String.valueOf(theStack.pop()); //valueGetKey(hashMap, theStack.pop());
									interAns=str1+str2+"+";
								}else{ //+-*
									String ttt = getEquaFromBegin(input.substring(0, j+2));
									j++;
									input = ttt+input.substring(j+1);
								}
							}
							break;
						case '-':
							char ch2 = input.charAt(j+1);
							char ch3 = input.charAt(j-1);
							char ch6 = input.charAt(j-2);
							if( String.valueOf(ch2).equals("*") || String.valueOf(ch2).equals("/") ){
								if( ch3>=65 && ch6>=65 && ch3!=94 && ch6!=94 ){
									str2 = String.valueOf(theStack.pop()); //valueGetKey(hashMap, theStack.pop());
									str1= String.valueOf(theStack.pop()); //valueGetKey(hashMap, theStack.pop());
									interAns=str1+str2+"-";
								}else{ //+-*
									String ttt = getEquaFromBegin(input.substring(0, j+2));
									j++;
									input = ttt+input.substring(j+1);
								}
							}
							break;
						case '*':
							char ch7 = input.charAt(j-2);
							if( String.valueOf(ch7).equals("+") || String.valueOf(ch7).equals("-") || String.valueOf(ch7).equals("*") || String.valueOf(ch7).equals("/") || String.valueOf(ch7).equals("^") ){
								interAns = "";
							}else{
								str2 = String.valueOf(theStack.pop()); //valueGetKey(hashMap, theStack.pop());
								str1= String.valueOf(theStack.pop()); //valueGetKey(hashMap, theStack.pop());
    							interAns=str1+str2+"*";
							}
							
							break;
						case '/':
							str2 = String.valueOf(theStack.pop()); //valueGetKey(hashMap, theStack.pop());
							str1= String.valueOf(theStack.pop()); //valueGetKey(hashMap, theStack.pop());
							interAns=str1+str2+"/";
							break;
						case '^': //94
							str2 = String.valueOf(theStack.pop()); //valueGetKey(hashMap, theStack.pop());
							str1= String.valueOf(theStack.pop()); //valueGetKey(hashMap, theStack.pop());
							interAns=str1+str2+"^";
							break;	

						} //end switch

						if( !interAns.equals("") ){
							hashMap.put("("+interAns+")",charKey);
							theStack.push(charKey);
							//charKey=(char)(charKey+1);

							input = input.replace(interAns, String.valueOf(charKey));

							//System.out.print(input+"\n\n");
							charKey=(char)(charKey+1);
							if( charKey == 94 )
								charKey=(char)(charKey+1);
							input = evaluatePostfixExpression(input, 0);
						}
					} //end else
				} //end for loop


				//=valueGetKey(hashMap, theStack.pop());
				//interAns=Double.parseDouble(str1);
			}//end of if
		} catch(Exception e) {
			JOptionPane.showMessageDialog(new Frame(), e.getMessage(), "Error parsing equations", JOptionPane.INFORMATION_MESSAGE);
		}
		return input;
	}




	private static String getEquaFromBegin(String substring) {

		char ch;
		String interAns = "";
		String str1,str2;
		Stack<Character> theStack1 = new Stack<Character>(); //重置栈,后缀表达式求值是将数字放入栈中
		try {        	
			for(int j=0; j<substring.length()-1; j++) {

				String[] a = substring.split("\\*|\\^|\\+|\\/|-|\\(|\\)");
				ArrayList<String> b= new ArrayList<String>();
				for(int i=0;i<a.length;i++)
					if(!a[i].equals(""))
						b.add(a[i]);

				if( b.size()*2+1 != substring.split("").length ){
					ch=substring.charAt(j);
					if(ch>=65 && ch!=94) {  //j代表一个double型的数字
						theStack1.push(ch);
					} else {

						switch(ch) {
						case '+':
							str2 = String.valueOf(theStack1.pop()); //valueGetKey(hashMap, theStack.pop());
							str1= String.valueOf(theStack1.pop()); //valueGetKey(hashMap, theStack.pop());
							interAns=str1+str2+"+";
							break;
						case '-':						
							str2 = String.valueOf(theStack1.pop()); //valueGetKey(hashMap, theStack.pop());
							str1= String.valueOf(theStack1.pop()); //valueGetKey(hashMap, theStack.pop());
							interAns=str1+str2+"-";
							break;
						case '*':
							str2 = String.valueOf(theStack1.pop()); //valueGetKey(hashMap, theStack.pop());
							str1= String.valueOf(theStack1.pop()); //valueGetKey(hashMap, theStack.pop());
							interAns=str1+str2+"*";
							break;
						case '/':
							str2 = String.valueOf(theStack1.pop()); //valueGetKey(hashMap, theStack.pop());
							str1= String.valueOf(theStack1.pop()); //valueGetKey(hashMap, theStack.pop());
							interAns=str1+str2+"/";
							break;
						case '^': //94
							str2 = String.valueOf(theStack.pop()); //valueGetKey(hashMap, theStack.pop());
							str1= String.valueOf(theStack.pop()); //valueGetKey(hashMap, theStack.pop());
							interAns=str1+str2+"^";
							break;	
						} //end switch

						if( !interAns.equals("") ){
							hashMap.put("("+interAns+")",charKey);
							theStack1.push(charKey);

							substring = substring.replace(interAns, String.valueOf(charKey));

							//System.out.print(substring+"\n\n");
							charKey=(char)(charKey+1);
							if( charKey == 94 )
								charKey=(char)(charKey+1);
							substring = getEquaFromBegin(substring);
						}
					} //end else
				} //end for if

			}//end of loop
			theStack.push((char) (charKey-1));
		} catch(Exception e) {
			System.out.println("please enter legal numbers!");
			e.printStackTrace();
		}

		return substring;
	}

	private static String valueGetKey(Map<String, Character> hashMap2,Character character) {
		Set set = hashMap2.entrySet();//新建一个不可重复的集合
		String arr = "";//新建一个集合
		Iterator it = set.iterator();//遍历的类
		while(it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();//找到所有key-value对集合
			if(entry.getValue().equals(character)) {//通过判断是否有该value值
				String s = (String)entry.getKey();//取得key值
				arr = s;
			}
		}
		return arr;
	}

	public String getPositivePart() {
		return positivePart;
	}

	public void setPositivePart(String positivePart) {
		this.positivePart = positivePart;
	}

	public String getNegativePart() {
		return negativePart;
	}

	public void setNegativePart(String negativePart) {
		this.negativePart = negativePart;
	}

	public static void main(String[] args) {
        String rxn = "max_a*(BasalExpression_a+(BasalExpression_a_0+I_a_0*(a/K_a_0)^N_a_0/(1+(a/K_a_0)^N_a_0))+(BasalExpression_a_1+I_a_1*(1/(1+(b/K_a_1)^N_a_1))))-deg_a*a+(BasalExpression_a_2+I_a_2*(c/K_a_2)^N_a_2/(1+(c/K_a_2)^N_a_2))";
    	//String internalStr1 = "(343-2*233*3+34*3444-3*(1/(34+90)))*3";
    	parseEquation a = new parseEquation(rxn);
   
       //System.out.print("the final result is :"+a.negativePart+"\n"+a.positivePart);
    }
}

