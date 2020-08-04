/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdktest;

import java.util.StringTokenizer;

/**
 *
 * @author asenf
 */
public class Entry implements Comparable<Entry> {
    private String chromosomeName;
    private int start, end;
    
    public Entry(String chromosomeName, int start, int end) {
        this.chromosomeName = chromosomeName;
        this.start = start;
        this.end = end;
    }
    
    /*
     * Read lines from a BED like file
     */
    public Entry(String bedLine) {
        StringTokenizer st = new StringTokenizer(bedLine);
        this.chromosomeName = st.nextToken();
        this.start = Integer.parseInt(st.nextToken());
        this.end = Integer.parseInt(st.nextToken());
    }

    public String getChromosomeName() {
        return this.chromosomeName;
    }
    
    public int getStart() {
        return this.start;
    }
    
    public int getEnd() {
        return this.end;                
    }

    /*
     * Produce BED like output
     */
    @Override
    public String toString() { 
        return String.format(this.getChromosomeName() + "\t" + this.start + "\t" + this.end); 
    }
    
    /*
     * Sort by chromosome first, then by start within chromosome
     */
    @Override
    public int compareTo(Entry secondEntry) {
        if (this.getChromosomeName().equalsIgnoreCase(secondEntry.getChromosomeName())) {
            return Integer.compare(this.getStart(), secondEntry.getStart());
        } else {
            return this.getChromosomeName().compareTo(secondEntry.getChromosomeName());
        }
    }
	
}
