package com.cintcm.hamster.relation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.chenlb.mmseg4j.example.Complex;
import com.cintcm.hamster.relation.io.excel.RelationRenderer;

public class HasCoreRelationExtractor {
	private String docId;
	// private int cursor = 0;
	private List<List<String>> words2;
	private List<String> words1;
	// private String subject;
	private List<Relation> relations = new ArrayList<Relation>();
	private String text;

	public HasCoreRelationExtractor(String txt, String docId) {
		this.text = txt;
		this.words2 = Utils.breakSentence(txt);
		System.out.println(words2);
		this.words1 = new ArrayList<String>();
		for (List<String> l : words2) {
			this.words1.addAll(l);
		}

		this.docId = docId;

		this.extractRelations();
	}

	private void extractRelations() {
		// System.out.println("extract relations at: " + c1);

		String s = null;

		for (int c1 = 0; c1 < words1.size(); c1++) {
			s = words1.get(c1);
			if (TCMCoreDecider.isCore(s)) {
				for (int c2 = 0; c2 < words1.size(); c2++) {

					String obj = words1.get(c2);
					if (obj.equalsIgnoreCase(s))
						continue;

					if (TCMNounDecider.isNoun(obj)) {
						boolean verbFound = false;

						int[] verb_nums = { c1 - 1, c1 + 1, c2 - 1, c2 + 1,
								c1 - 2, c1 + 2, c2 - 2, c2 + 2 };

						for (int verb_num : verb_nums) {
							if ((verb_num >= 0) && (verb_num < words1.size())) {
								String pred = words1.get(verb_num);
								if (VerbDecider.isVerb(pred)
										&& !pred.equalsIgnoreCase(s)
										&& !pred.equalsIgnoreCase(obj)) {
									// System.out.println(1/(c2-c1+1));
									Relation rel = new Relation(s, pred, obj)
											.setDocId(this.docId).setText(
													this.text);
									if (TCMCoreDecider.isCore(obj)) {
										rel.setValue(2);
									} else {
										rel.setValue(1.0 / (double) (Math
												.abs(c2 - c1) + 1));
									}

									verbFound = true;
									System.out.println(rel);
									relations.add(rel);
								}
							}
							if (!verbFound) {
								Relation rel = new Relation(s, "", obj)
										.setDocId(this.docId)
										.setText(this.text);
								rel.setValue(0);
								System.out.println(rel);
								relations.add(rel);
							}
							
						}

					}

				}

			}

		}

	}

	public List<Relation> getRelations() {
		return relations;
	}

	public static List<Relation> getRelations(File file, String docId) {
		String[] sentences = Utils.breakFileIntoSentences(file);
		List<Relation> relations = new ArrayList<Relation>();

		for (String sentence : sentences) {
			relations.addAll(new HasCoreRelationExtractor(sentence, docId)
					.getRelations());
		}

		return relations;
	}

	public static List<Relation> getRelationsFromExcel(File exl) {
		List<Relation> relations = new ArrayList<Relation>();
		FileInputStream fIn;
		try {
			fIn = new FileInputStream(exl);
			HSSFWorkbook readWorkBook = new HSSFWorkbook(fIn);
			HSSFSheet readSheet = readWorkBook.getSheet("docs");

			Iterator<Row> it = readSheet.rowIterator();
			while (it.hasNext()) {
				Row row = it.next();
				String text = row.getCell(0).getStringCellValue();
				String docId = row.getCell(1).getStringCellValue();
				String[] sentences = Utils.breakParagraphIntoSentences(text);
				for (String sentence : sentences) {
					relations.addAll(new HasCoreRelationExtractor(sentence,
							docId).getRelations());
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return relations;
	}

	public static void main(String[] args) {
		String txt = "人参有“补五脏、安精神、定魂魄、止惊悸、除邪气、明目开心益智”的功效,“久服轻身延年”";
		String doc = "神农本草经";
		File outputFile = new File("data/relations/人参功效.xls");

		HasCoreRelationExtractor extractor = new HasCoreRelationExtractor(txt,
				doc);
		List<Relation> rels = extractor.getRelations();
		new RelationRenderer(rels, outputFile).outputFile();

	}

}
