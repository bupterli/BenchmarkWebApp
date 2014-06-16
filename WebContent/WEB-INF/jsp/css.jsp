 <!-- embedded css script for manual css alteration or fixes -->
 <style>
 
 	html {
 	 /* prevent body from shifting when scroll appears after content is generated */ 
 	 overflow-y: scroll; 
 	}

   #chart_legend {	
   	/* increase whitespace at bottom of chart legend */
   	padding-bottom: 30px;
   }
   
   #chart_div { 
   	/* increase marign at top of chart area */
   	margin-top: 30px;
   }
   
   .inline { 
   	/* shortcut to change an element's display from "block" to "inline" */
   	display: inline;
   }
   
   .right {
    /* shortcut to change an element's alignment */
    text-align: right;
   }
   
   .center {
    /* shortcut to center element*/
    text-align: center;
   }
   
   .line_break { 
   	/* creates 10px of whitespace at top of each element */
   	margin-top:10px;
   	display: block;
   }
   
   .accordion .accordion-heading .accordion-toggle {
   	/* change font color and size for collapsible divs */
	color: #000;
	font-size:24px;
   }
   
   .btn-toolbar {
    /* fix top spacing and button alignment for a group of buttons*/
   	margin-top: 0px;
   	text-align: left;
   }
   
   .btn-group {
    /* fix extra space on left of button group */
   	margin-left: 0px !important;
   }
   
   .filters {
    /* fix chart filter alignment */
   	text-align: right;
   }
   
   table {
    /* fixes for overflowing text in table*/
   	table-layout: fixed;
   	width: 100%;
   	word-wrap: break-word;   		
   }
   
 	table th, table td { 
 	 /* fix html table overflow not shown when it is collapsible */
 	 overflow: auto; 
 	}
 	
 	.visible {
 	 /* fix html table overflow not shown when it is collapsible */
 	 overflow: visible; 
 	}
 	
 	.modal form {
 	 /* fix bleeding out of any forms within a modal */
 	 margin: 0 0 0px; 
 	}
 	
 	#chart_div svg g g g:nth-child(2) {
	 /* bar chart: change cursor to a hand when onmouseover */
	 cursor: pointer;
	}
	
	.pie path {
	 /* pie chart: change cursor to a hand when onmouseover */
	 cursor: pointer;
	}
	
	.caret {
	 /* fix dropdown down arrow color on top bar */
	 border-top-color: white;
	 border-bottom-color: white;
	}
 </style>