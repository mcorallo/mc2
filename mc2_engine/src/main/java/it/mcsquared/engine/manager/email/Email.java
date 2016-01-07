package it.mcsquared.engine.manager.email;

import java.io.File;
import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class Email implements Serializable {

	private String from;
	private List<String> tos;
	private List<String> ccs;
	private List<String> bccs;
	private String subject;
	private String Body;
	private List<File> attachments;
	private boolean html = true;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public List<String> getTos() {
		return tos;
	}

	public void setTos(List<String> tos) {
		this.tos = tos;
	}

	public List<String> getCcs() {
		return ccs;
	}

	public void setCcs(List<String> ccs) {
		this.ccs = ccs;
	}

	public List<String> getBccs() {
		return bccs;
	}

	public void setBccs(List<String> bccs) {
		this.bccs = bccs;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return Body;
	}

	public void setBody(String body) {
		Body = body;
	}

	public List<File> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<File> attachments) {
		this.attachments = attachments;
	}

	public boolean isHtml() {
		return html;
	}

	public void setHtml(boolean html) {
		this.html = html;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Email [from=");
		builder.append(from);
		builder.append(", tos=");
		builder.append(tos);
		builder.append(", ccs=");
		builder.append(ccs);
		builder.append(", bccs=");
		builder.append(bccs);
		builder.append(", subject=");
		builder.append(subject);
		builder.append(", Body=");
		builder.append(Body);
		builder.append(", attachments=");
		builder.append(attachments);
		builder.append(", html=");
		builder.append(html);
		builder.append("]");
		return builder.toString();
	}
}
