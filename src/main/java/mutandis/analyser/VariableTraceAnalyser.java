package mutandis.analyser;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import mutandis.exectionTracer.JSFuncExecutionTracer;
import mutandis.exectionTracer.JSVarExecutionTracer;

import com.crawljax.util.Helper;


/**
 * this class is for reading variables trace files and recording the number of times it has been read
 * @author shabnamm
 *
 */
public class VariableTraceAnalyser {
	/* function1->variable1,variable2,... function2->variablen1,variablen2,... ... */
	private TreeMap<String, List<Variable>> variableMap=new TreeMap<String, List<Variable>>();
	private List<String> traceFilenameAndPath;
	private String 	resultFilenameAndPath;
	private String outputFolder;
	
	public VariableTraceAnalyser(String outputFolder){
		this.outputFolder=Helper.addFolderSlashIfNeeded(outputFolder);
		resultFilenameAndPath=this.outputFolder + "variableTraceResult.txt";
		traceFilenameAndPath=allTraceFiles();
		
	}
	
	public void startAnalysingTraceFiles(){
		try{
			List<String>filenameAndPathList=getTraceFilenameAndPath();
			for (String filenameAndPath:filenameAndPathList){
				BufferedReader input =
					new BufferedReader(new FileReader(filenameAndPath));
				
				String line="", functionName="";
			  while ((line = input.readLine()) != null){
				
				if ("".equals(line))
					break;
				
			
				String[] funcInfo=line.split("::");
			//	functionName=funcInfo[0]+"::"+funcInfo[1];
				functionName=funcInfo[1];
				while (!(line = input.readLine()).equals("================================================")){
					
									
					String[] varInfo=line.split("::");
					String varName=varInfo[0], varType=varInfo[1], 
					varCategory=varInfo[2], statementCategory=varInfo[3], sourceCode=funcInfo[2];
					
					if (variableMap.containsKey(functionName)){
						List<Variable> varList=variableMap.get(functionName);        
				
						int matchedVarIndex=-1;
						for (int i=0;i<varList.size();i++){
							if (varName.equals(varList.get(i).getVarName())){
								matchedVarIndex=i;
					
								break;
							}
						}
				
						if (matchedVarIndex==-1){
					
							Variable newVar=new Variable(varName, varType, varCategory, statementCategory, sourceCode);
							varList.add(newVar);
							variableMap.put(functionName,varList);
						}
				
						else{
					
							Variable matchedVar=varList.get(matchedVarIndex);
							matchedVar.addVarInfo(varType, varCategory, statementCategory, sourceCode);
							varList.set(matchedVarIndex, matchedVar);
					
						}
					}
					
					else{
						Variable newVar=new Variable(varName, varType, varCategory, statementCategory, sourceCode);
						List<Variable> newVarList=new ArrayList<Variable>();
						newVarList.add(newVar);
						variableMap.put(functionName,newVarList);
					}
				}
				
			  }
			}
			
			writingResultsintoFile();
			
			
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public TreeMap<String, List<Variable>> getVariableMap(){
		return variableMap;
	}
	
	public List<String> getTraceFilenameAndPath() {
		return traceFilenameAndPath;
	}
	
	
	
	private void writingResultsintoFile(){
		
		try{
			PrintWriter file = new PrintWriter(resultFilenameAndPath);
			file.write(getResult());
			file.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getResult(){
		
		StringBuffer result = new StringBuffer();
		
		Set <String> keySet=variableMap.keySet();
		Iterator<String> it=keySet.iterator();
		while(it.hasNext()){
			
			String funcAndPath=it.next();
	//		result.append("functionName::" + funcAndPath +"\n");
			List<Variable> varList=variableMap.get(funcAndPath);
			for(Variable var:varList){
				result.append("functionName::" + funcAndPath + "\n");
				result.append("variableName::" + var.getVarName() + "\n");
				int noTotalUsage=getNoTotalUsage(funcAndPath);
				var.setUsagePercentage(noTotalUsage);
				int totalVars=varList.size();
				double threshold=(double)1/totalVars;
				var.setThreshold(threshold);
				result.append("usage::" + var.getUsagePercentage(noTotalUsage) + "\n");
				ArrayList<ArrayList<String>> varinfoList=var.getVarInfo();
				for (ArrayList<String> varinfo:varinfoList){
					
					String varType=varinfo.get(0), varCategory=varinfo.get(1),
									statementCategory=varinfo.get(2), sourceCode=varinfo.get(3);
					
					result.append(varType + "::" + varCategory + "::"
							+ statementCategory + "::" + sourceCode);
					result.append("\n");
					
				}
				
				result.append("================================================" + "\n");
				
				
				
			}
			
			
			
		}
		
		return result.toString();
		
		
	}
	
	public int getNoTotalUsage(String funcName){
		
		int noTotalUsage=0;
		List<Variable> varList=variableMap.get(funcName);
		for(Variable var:varList){
			
			noTotalUsage+=var.getUsage();
			
		}
		
		return noTotalUsage;
		
	}
	
	private List<String> allTraceFiles() {
		ArrayList<String> result = new ArrayList<String>();

		/* find all trace files in the trace directory */
		File dir = new File(outputFolder + JSVarExecutionTracer.EXECUTIONTRACEDIRECTORY);

		String[] files = dir.list();
		if (files == null) {
			return result;
		}
		for (String file : files) {
			if (file.endsWith(".txt")) {
				result.add(outputFolder + JSVarExecutionTracer.EXECUTIONTRACEDIRECTORY + file);
			}
		}

		return result;
	}
	
	

	

}
