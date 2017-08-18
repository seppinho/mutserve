package genepi.mut.validate;

import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;

public class FailureNotification extends WorkflowStep {

	@Override
	public boolean run(WorkflowContext context) {

		Object mail = context.getData("cloudgene.user.mail");
		Object name = context.getData("cloudgene.user.name");
		String stepObject = context.getData("cloudgene.failedStep.classname").toString();

		if (stepObject == null) {
			context.println("No error message sent. Object is empty");
			return true;
		}

		if (mail != null && !mail.equals("")) {

			String subject = "Job " + context.getJobName() + " failed.";
			String message = "Dear "
					+ name
					+ ",\n"
					+ "unfortunately, your job failed. "
					+ "\n\nMore details about the error can be found on https://mtdna-server.uibk.ac.at/start.html#!jobs/"
					+ context.getJobName();

			try {
				context.sendMail(subject, message);

				if (!stepObject.equals(InputValidation.class.getName())) {

					// send all errors after input validation to us

					context.sendMail("sebastian.schoenherr@uibk.ac.at", subject
							+ " [" + stepObject + "]", message);

					context.sendMail("hansi.weissensteiner@i-med.ac.at", subject + " ["
							+ stepObject + "]", message);
				}

				context.ok("We have sent an email to <b>" + mail
						+ "</b> with the error message.");
				context.println("We have sent an email to <b>" + mail
						+ "</b> with the error message.");
				return true;
			} catch (Exception e) {
				context.error("Sending error message failed: " + e.getMessage());
				context.println("Sending error message failed: "
						+ e.getMessage());
				return false;
			}

		} else {
			context.ok("No email failure notifications are sent for mtDNA-Server's public mode");
			return true;
		}

	}

}
