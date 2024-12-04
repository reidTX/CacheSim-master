import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.HashSet;
public class milestone1 {
  static int virtualPagesMapped = 0;
  static int pageTableHits = 0;
  static int pagesFromFree = 0;
  static int totalPageFaults = 0;
 public static void main(String[] args) {
  String[] trace_file = new String[3];
  int cache_size = 0;
  int block_size = 0;
  int associativity = 0;
  String replacement_policy = null;
  String memory_size = null;
  double percent_memory_used = 0;
  int instructions_per_time_slice = 0;
  int traceLen = 0;
  int x = 0;
  for (int i = 0; i < args.length; i++) {
   switch (args[i]) {
    case "-f":
     if (i + 1 < args.length) {
      trace_file[x] = args[i + 1];
      i++;
      x++;
     }
     break;
    case "-s":
     if (i + 1 < args.length) {
      cache_size = Integer.parseInt(args[i + 1]);
      i++;
     }
     break;
    case "-b":
     if (i + 1 < args.length) {
      block_size = Integer.parseInt(args[i + 1]);
      i++;
     }
     break;
    case "-a":
     if (i + 1 < args.length) {
      associativity = Integer.parseInt(args[i + 1]);
      i++;
     }
     break;
    case "-r":
     if (i + 1 < args.length) {
      replacement_policy = args[i + 1];
      i++;
     }
     break;
    case "-p":
     if (i + 1 < args.length) {
      memory_size = args[i + 1];
      i++;
     }
     break;
    case "-u":
     if (i + 1 < args.length) {
      percent_memory_used = Double.parseDouble(args[i + 1]);
      i++;
     }
     break;
    case "-n":
     if (i + 1 < args.length) {
      instructions_per_time_slice = Integer.parseInt(args[i + 1]);
      i++;
     }
     break;
   }
  }
  System.out.println("Cache Simulator - CS 3853 Fall 2024 - Group#2\n");
  System.out.println("Trace Files:");
  for (String file : trace_file) {
   if (file != null) {
    System.out.println("\t"+file);
   }
  }
  System.out.println("\n***** Cache Input Parameters ******");
  System.out.println("Cache Size: \t\t"+cache_size+" KB");
  System.out.println("Block Size: \t\t"+block_size+" bytes");
  System.out.println("Associativity: \t\t"+associativity);
  System.out.println("Replacement Policy: \t"+formatReplacementPolicy(replacement_policy));
  System.out.println("Physical Memory: \t" + memory_size);
  System.out.println("Percent Memory Used by System: " + percent_memory_used + "%");
  System.out.println("Instructions / Time Slice: " + instructions_per_time_slice+"\n");
  if(trace_file[0] != null) { 
   traceLen++;
   } 
   if(trace_file[1] != null) { 
   traceLen++;
   } 
   if(trace_file[2] != null) { 
   traceLen++; 
   } 
  if(trace_file[0] != null) {
   processTrace(trace_file[0],cache_size,block_size,associativity,replacement_policy,memory_size,percent_memory_used,instructions_per_time_slice,traceLen);
  }
  if(trace_file[1] != null) {
   processTrace(trace_file[1],cache_size,block_size,associativity,replacement_policy,memory_size,percent_memory_used,instructions_per_time_slice,traceLen);
  }
  if(trace_file[2] != null) {
   processTrace(trace_file[2],cache_size,block_size,associativity,replacement_policy,memory_size,percent_memory_used,instructions_per_time_slice,traceLen);
  }
 }
  private static String formatReplacementPolicy(String replacementPolicy) {
  switch (replacementPolicy.toLowerCase()) {
   case "rr":
    return "Round Robin";
   case "lfu":
    return "Least Frequently Used";
   case "lru":
    return "Least Recently Used";
   default:
    return replacementPolicy;
  }
 }
 public static void processTrace(String trace_file,int cache_size,int block_size,int associativity,String replacement_policy,String memory_size, double percent_memory_used, int instructions_per_slice, int traceLen) {
  int total_block = ((cache_size*1024)/block_size);
  int rows_num = total_block / associativity;
  int index_size = (int) (Math.log(rows_num) / Math.log(2));
  int offset_size = (int) (Math.log(block_size)/Math.log(2));
  int tag_size = (int) (32-index_size-offset_size);
  int overhead_size = (rows_num*(tag_size+1)/2);
  int implement_memSizeb = ((cache_size*1024)+overhead_size);
  double implement_memSizekb = implement_memSizeb / 1024;
  double cost_perkb = 0.15;
  double cost = Math.round((implement_memSizekb * cost_perkb)*100.0)/100.0;
  int hits = 0;
  int misses = 0;
  int compulsoryMisses = 0;
  int conflictMisses = 0;
  HashMap<Integer, String> cache = new HashMap<>();
  int[] hitMiss = {0, 0};
  int[] missType = {0,0};// missType[0]= compulsoryMisses and missType[1]= conflictMisses
  int[] accessBytes = {0,0,0};//0 = total accesses 1= instruction bytes total 2 is unique addresses
  fileRead(trace_file,block_size,rows_num,cache,hitMiss,missType,accessBytes, new HashMap<>());
  System.out.println("***** Cache Calculated Values *****\n");
  System.out.println("Total # Blocks: \t\t"+total_block);
  System.out.println("Tag Size: \t\t\t"+tag_size+" bits");
  System.out.println("Index Size: \t\t\t"+index_size+" bits");
  System.out.println("Total # Rows: \t\t\t"+rows_num);
  System.out.println("Overhead Size: \t\t\t"+overhead_size+" bytes");
  System.out.println("Implementation Memory Size: \t"+implement_memSizekb+" KB ("+implement_memSizeb+" bytes)");
  System.out.println("Cost: \t\t\t\t$"+cost+" @ ($"+cost_perkb+" / KB)");
  int physical_pages = (Integer.parseInt(memory_size)* 1024*1024) / (4*1024);
  int system_pages = (int) (physical_pages * (percent_memory_used/100.0));
  int entry_bits=(int) (Math.log(physical_pages)/Math.log(2)) + 1;
  int total_ram = (512*1024*traceLen*entry_bits/8);
  System.out.println("\n***** Physical Memory Calculated Values *****\n");
  System.out.println("Number of Physical Pages: \t" + physical_pages);
  System.out.println("Number of Pages for System: \t" + system_pages);
  System.out.println("Size of Page Table Entry: \t" + entry_bits + " bits");
  System.out.println("Total RAM for Page Table(s): \t" + total_ram + " bytes\n");
  System.out.println("\n***** CACHE SIMULATION RESULTS *****\n");
  System.out.println("Total Cache Accesses:\t"+accessBytes[0]+"   ("+accessBytes[2]+" addresses)");
  System.out.println("Instruction Bytes:   \t"+accessBytes[1]);
  System.out.println("Cache Hits: \t" + hitMiss[0]);
  System.out.println("Cache Misses: \t" + hitMiss[1]);
  System.out.println("--- Compulsory Misses: \t" + missType[0]);
  System.out.println("--- Conflict Misses \t" + missType[1]);
  double hitrate = (100.0*hitMiss[0]/(hitMiss[1]+hitMiss[0]));
  int unusedBlocks = total_block - missType[0];
  double unusedSpaceKB = (unusedBlocks * block_size) / 1024.0; 
  double unusedSpacePercentage = (unusedSpaceKB / cache_size) * 100.0; 
  unusedSpaceKB = Math.round(unusedSpaceKB * 100.0) / 100.0;
  unusedSpacePercentage = Math.round(unusedSpacePercentage * 100.0) / 100.0;
  int hit_cycles = 1;
  int num_reads = (int) Math.ceil(block_size / 4.0);
  int miss_penalty = 4 * num_reads;
  double cpi = ((hitMiss[0] * hit_cycles) + (hitMiss[1] * miss_penalty)) / (double) accessBytes[0];
  cpi = Math.round(cpi * 100.0) / 100.0;
  System.out.println("\n***** CACHE HIT & MISS RATE: *****\n");
  System.out.println("Hit Rate:\t\t\t"+hitrate+"%");
  System.out.println("Miss Rate:\t\t\t"+(100-hitrate)+"%");
  System.out.println("CPI: \t\t\t\t" + cpi + " Cycles/Instruction");
  System.out.println("Unused Cache Space: \t\t" + unusedSpaceKB + " KB / " + cache_size + " KB = " + unusedSpacePercentage + "%");
  System.out.println("Unused Cache Blocks: \t\t" + unusedBlocks + " / " + total_block);
  System.out.println("\n***** PHYSICAL MEMORY SIMULATION RESULTS: *****\n");
  System.out.println("Physical Pages Used By SYSTEM: \t"+system_pages);
  System.out.println("Pages Available to User: \t"+(physical_pages-system_pages));
  System.out.println("Virtual Pages Mapped: \t"+(virtualPagesMapped));
  System.out.println("------------------------------");
  System.out.println("Page Table Hits: \t"+(pageTableHits));
  System.out.println("Pages from Free: \t"+(pagesFromFree));
  System.out.println("Total Page Faults:\t"+(totalPageFaults));
 }
 public static int fileRead(String trace_file, int block_size, int rows, HashMap<Integer, String> cache, int[] hitMiss,int[] missType,int[] accessBytes, HashMap<Integer,Integer> accessHistory) {
   int hits = 0;
   int misses = 0;
   int compulsoryMisses = 0;
   int conflictMisses = 0;
   virtualPagesMapped = 0;
   pageTableHits = 0;
   pagesFromFree = 0;
   totalPageFaults = 0;
try {
        BufferedReader br = new BufferedReader(new FileReader(trace_file));
        String line;
        Pattern pattern = Pattern.compile("EIP \\((\\d+)\\): ([0-9a-fA-F]+)");
        
        // Use a Set to track unique page addresses
        HashSet<Long> uniquePageAddresses = new HashSet<>();

        while ((line = br.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                int instructionBytes = Integer.parseInt(matcher.group(1));
                String hexValue = matcher.group(2);
                accessBytes[1] += instructionBytes;
                long address = Long.parseLong(hexValue, 16);

                // Calculate page number (assuming 4KB pages)
                long pageNumber = address / (4 * 1024);

                // Track virtual pages mapped
                if (!uniquePageAddresses.contains(pageNumber)) {
                    virtualPagesMapped++;
                    uniquePageAddresses.add(pageNumber);
                    pagesFromFree++;
                }

                int index = (int) ((address / block_size) % rows);
                String tag = Long.toHexString(address / (block_size * rows));

                if (!accessHistory.containsKey(index)) {
                    compulsoryMisses++;
                    misses++;
                    
                    cache.put(index, tag);
                    accessHistory.put(index, 1); 
                } else if (cache.containsKey(index) && !cache.get(index).equals(tag)) {
                    conflictMisses++;
                    misses++;
                    cache.put(index, tag);
                } else {
                    hits++;
                    pageTableHits++;
                }
            }
        }
        br.close();

        // Additional tracking to match expected output
        virtualPagesMapped = uniquePageAddresses.size();
        pageTableHits = hits;
        pagesFromFree = uniquePageAddresses.size();
        totalPageFaults = misses;

    } catch (IOException e) {
        e.printStackTrace();
    }
    
    hitMiss[0] = hits;
    hitMiss[1] = misses;
    missType[0] = compulsoryMisses;
    missType[1] = conflictMisses;
    accessBytes[0] = hits + misses;
    accessBytes[2] = cache.size();
    
    return hits+misses+compulsoryMisses+conflictMisses+virtualPagesMapped+pageTableHits+pagesFromFree+totalPageFaults;
}
}