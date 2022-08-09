package hw2;
//Chenjie Xiong
import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.basic.BasicComboBoxUI.ItemHandler;

import hw1.Catalog;
import hw1.Database;
import hw1.Field;
import hw1.HeapFile;
import hw1.Tuple;
import hw1.TupleDesc;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;

public class Query {

	private String q;

	public Query(String q) {
		this.q = q;
	}

	public Relation execute() {
		Statement statement = null;
		try {
			statement = CCJSqlParserUtil.parse(q);
		} catch (JSQLParserException e) {
			System.out.println("Unable to parse query");
			e.printStackTrace();
		}
		Select selectStatement = (Select) statement;
		PlainSelect sb = (PlainSelect) selectStatement.getSelectBody();

		// your code here
		Catalog catalog = Database.getCatalog();

		// get the origin table
		Table curTable = (Table) sb.getFromItem();
		String curTableName = curTable.getName();
		int curTableId = catalog.getTableId(curTableName);
		HeapFile curTableHeapFile = catalog.getDbFile(curTableId);
		TupleDesc curTupleDesc = curTableHeapFile.getTupleDesc();
		ArrayList<Tuple> curTuples = curTableHeapFile.getAllTuples();
		Relation curRelation = new Relation(curTuples, curTupleDesc);

		// deal with join
		List<Join> joins = sb.getJoins();
		if (joins != null) {
			for (Join join : joins) {
				Table joinTable = (Table) join.getRightItem();
				String joinTableName = joinTable.getName();
				int joinTableId = catalog.getTableId(joinTableName);
				TupleDesc joinDesc = catalog.getTupleDesc(joinTableId);
				ArrayList<Tuple> joinTuples = catalog.getDbFile(joinTableId).getAllTuples();
				Relation joinRelation = new Relation(joinTuples, joinDesc);

				// deal with on expression
				String[] joinTableFieldStrings = join.getOnExpression().toString().split("=");
				String[] joinTableNameFieldLeft = joinTableFieldStrings[0].trim().split("\\.");
				String[] joinTableNameFieldRight = joinTableFieldStrings[1].trim().split("\\.");
				String fieldNameLeft = joinTableNameFieldLeft[1];
				String fieldNameRight = joinTableNameFieldRight[1];

				if (!joinTableName.toLowerCase().equals(joinTableNameFieldRight[0].toLowerCase())) {
					String temp = fieldNameLeft;
					fieldNameLeft = fieldNameRight;
					fieldNameRight = temp;
				}
				int fieldIdLeft = curRelation.getDesc().nameToId(fieldNameLeft);
				int fieldIdRight = joinRelation.getDesc().nameToId(fieldNameRight);
				// perform join
				curRelation = curRelation.join(joinRelation, fieldIdLeft, fieldIdRight);

			}

		}
		
		// deal with where
		Expression whereExpression = sb.getWhere();
		if (whereExpression != null) {
			WhereExpressionVisitor whereExpressionVisitor = new WhereExpressionVisitor();
			whereExpression.accept(whereExpressionVisitor);
			String leftFieldString = whereExpressionVisitor.getLeft();
			Field rightField = whereExpressionVisitor.getRight();
			curRelation = curRelation.select(curRelation.getDesc().nameToId(leftFieldString),
					whereExpressionVisitor.getOp(), rightField);

		}
		//deal with select(project)
		
		List<SelectItem> selectExpression = sb.getSelectItems();
		ArrayList<Integer> projectFieldId = new ArrayList<>(); 
		ColumnVisitor cVisitor = new ColumnVisitor();
		boolean isGroupby = sb.getGroupByColumnReferences() == null;
		for (SelectItem selectItem : selectExpression) {
			
			selectItem.accept(cVisitor);
			String selectCol = cVisitor.isAggregate() ? selectItem.toString() : cVisitor.getColumn(); 
			if (selectCol.equals("*")) {
			
				for (int i = 0; i < curRelation.getDesc().numFields(); i++) {
					projectFieldId.add(i);
				}
				break;
			}
			int fieldId = cVisitor.getColumn().equals("*") && cVisitor.isAggregate() ? 0: curRelation.getDesc().nameToId(cVisitor.getColumn());
			if (!projectFieldId.contains(fieldId)) projectFieldId.add(fieldId);
			
		}
		//deal with aggregate in select
		
		if (cVisitor.isAggregate()) {
			if (sb.getGroupByColumnReferences() == null) {
				curRelation = curRelation.project(projectFieldId);
				return curRelation.aggregate(cVisitor.getOp(), false);
			}
			
			curRelation = curRelation.aggregate(cVisitor.getOp(), sb.getGroupByColumnReferences() != null);
			
		}
		curRelation = curRelation.project(projectFieldId);
		

		return curRelation;

	}
}
