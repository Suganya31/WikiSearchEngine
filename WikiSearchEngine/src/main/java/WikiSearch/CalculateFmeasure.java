package WikiSearch;

import java.util.List;
import java.util.Set;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.aksw.qa.commons.measure.AnswerBasedEvaluation;

public class CalculateFmeasure {

	static List<IQuestion> questions;

	static Set<String> systemAnswers = null;
	//static List<IQuestion> questions;
	public static void main(String[] args) {
		//question ??? QALD7_Train_Multilingual
		//for (Dataset d : Dataset.values()) {
				//questions = LoaderController.load(d);
				questions = LoaderController.load(Dataset.QALD7_Train_Multilingual);
				System.out.println("Data loaded successfully");
				for (IQuestion q : questions) {
					systemAnswers=q.getGoldenAnswers();
					//systemAnswers.addAll(q.getGoldenAnswers());

					System.out.println(q.getLanguageToQuestion().get("en"));
					System.out.println(q.getGoldenAnswers());

					//System.out.println(AnswerBasedEvaluation.fMeasure(systemAnswers,q));
				}
			//}
	// systemAnswers should be the set of the URI returned for the questions
		
	}

}
