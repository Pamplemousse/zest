/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.zaproxy.zest.core.v1;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** The Class ZestScript. */
public class ZestScript extends ZestStatement implements ZestContainer {

    /** The Zest version implemented. */
    public static final String VERSION = "0.15";

    /** The URL for more info. */
    public static final String ZEST_URL = "https://github.com/zaproxy/zest/";

    /** A standard 'about' message to be included in all scripts. */
    public static final String ABOUT =
            "This is a Zest script. For more details about Zest visit " + ZEST_URL;

    /**
     * The type of the script: Active - the script will try to actively find vulnerabilities in the
     * request/response passed into it, ie it will perform attacks Passive - the script will try to
     * passively find vulnerabilities in the request/response passed into it, ie it will not make
     * any additional requests StandAlone - the script acts on a target specified in the script
     * (which can be overriden) the script will make requests Targeted - the script acts on a
     * request passed into it - it may make additional requests and may changes to that request
     * before submitting it. It may also not submit the request at all.
     */
    public enum Type {
        Active,
        Passive,
        StandAlone,
        Targeted;

        static Type getType(String type) {
            if (type == null || type.isEmpty()) {
                return null;
            }

            switch (type.toLowerCase(Locale.ROOT)) {
                case "active":
                    return Active;
                case "passive":
                    return Passive;
                case "standalone":
                    return StandAlone;
                case "targeted":
                    return Targeted;
                default:
                    return null;
            }
        }
    }

    /** The about. */
    private String about = ABOUT;

    /** The zest version. */
    private String zestVersion = VERSION;

    /** The generated by. */
    private String generatedBy;

    /** The author. */
    private String author;

    /** The title. */
    private String title;

    /** The description. */
    private String description;

    /** The prefix. */
    private String prefix;

    /** The type. */
    private String type;

    private transient Type scriptType;

    /** The parameters. */
    private ZestVariables parameters = new ZestVariables();

    /** The statements. */
    private List<ZestStatement> statements = new ArrayList<>();

    /** The authentication. */
    private List<ZestAuthentication> authentication = new ArrayList<>();

    /** Instantiates a new zest script. */
    public ZestScript() {
        super();
    }

    /**
     * Instantiates a new zest script.
     *
     * @param title the title
     * @param description the description
     * @param type the type
     */
    public ZestScript(String title, String description, String type) {
        this();
        this.title = title;
        this.description = description;
        this.setType(type);
    }

    /**
     * Instantiates a new zest script.
     *
     * @param title the title
     * @param description the description
     * @param type the type
     */
    public ZestScript(String title, String description, Type type) {
        this();
        this.title = title;
        this.description = description;
        this.setType(type);
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(Type type) {
        this.type = type.name();
        this.scriptType = type;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(String type) {
        setTypeImpl(type);
        if (scriptType == null) {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    private void setTypeImpl(String type) {
        scriptType = Type.getType(type);
        if (scriptType != null) {
            this.type = scriptType.name();
        }
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    @Override
    protected void init() {
        super.init();

        setTypeImpl(type);
    }

    @Override
    public ZestScript deepCopy() {
        ZestScript script = new ZestScript();
        this.duplicateTo(script);
        return script;
    }

    /**
     * Duplicate to.
     *
     * @param script the script
     */
    public void duplicateTo(ZestScript script) {
        script.about = this.about;
        script.zestVersion = this.zestVersion;
        script.generatedBy = this.generatedBy;
        script.author = this.author;
        script.title = this.title;
        script.description = this.description;
        script.prefix = this.prefix;
        script.parameters = this.parameters.deepCopy();
        script.type = this.type;
        script.scriptType = this.scriptType;

        for (ZestStatement zr : this.getStatements()) {
            script.add(zr.deepCopy());
        }
        for (ZestAuthentication za : this.getAuthentication()) {
            script.addAuthentication((ZestAuthentication) za.deepCopy());
        }
        script.setParameters(this.getParameters().deepCopy());
    }

    /**
     * Adds the statement to the end of the script.
     *
     * @param stmt the statement to add
     */
    public void add(ZestStatement stmt) {
        this.add(this.statements.size(), stmt);
    }

    /**
     * Adds the statement in the specified index in the script.
     *
     * @param index the index at which the statement will be added
     * @param stmt the statement to add
     */
    public void add(int index, ZestStatement stmt) {
        ZestStatement prev = this;
        if (index == this.statements.size()) {
            // Add at the end
            this.statements.add(stmt);

        } else {
            this.statements.add(index, stmt);
        }
        if (index > 0) {
            prev = this.statements.get(index - 1);
        }
        // This will wire everything up
        stmt.insertAfter(prev);
        checkStatementIndexes();
    }

    @Override
    public void move(int index, ZestStatement req) {
        this.remove(req);
        this.add(index, req);
        checkStatementIndexes();
    }

    /**
     * Removes the.
     *
     * @param req the req
     */
    public void remove(ZestStatement req) {
        this.statements.remove(req);
        req.remove();
        checkStatementIndexes();
    }

    /**
     * Removes the statement.
     *
     * @param index the index
     */
    public void removeStatement(int index) {
        this.remove(this.statements.get(index));
        checkStatementIndexes();
    }

    @Override
    public ZestStatement getStatement(int index) {
        checkStatementIndexes();
        for (ZestStatement zr : this.getStatements()) {
            if (zr.getIndex() == index) {
                return zr;
            }
            if (zr instanceof ZestContainer) {
                ZestStatement stmt = ((ZestContainer) zr).getStatement(index);
                if (stmt != null) {
                    return stmt;
                }
            }
        }

        return null;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the statements.
     *
     * @return the statements
     */
    public List<ZestStatement> getStatements() {
        return statements;
    }

    @Override
    public List<ZestStatement> getChildren() {
        return Collections.unmodifiableList(this.getStatements());
    }

    /**
     * Sets the statements.
     *
     * @param statements the new statements
     */
    public void setStatements(List<ZestStatement> statements) {
        this.statements = statements;
    }

    /**
     * Adds the authentication.
     *
     * @param auth the auth
     */
    public void addAuthentication(ZestAuthentication auth) {
        this.authentication.add(auth);
    }

    /**
     * Removes the authentication.
     *
     * @param auth the auth
     */
    public void removeAuthentication(ZestAuthentication auth) {
        this.authentication.remove(auth);
    }

    /**
     * Gets the authentication.
     *
     * @return the authentication
     */
    public List<ZestAuthentication> getAuthentication() {
        return authentication;
    }

    /**
     * Sets the authentication.
     *
     * @param authentication the new authentication
     */
    public void setAuthentication(List<ZestAuthentication> authentication) {
        this.authentication = authentication;
    }

    /**
     * Gets the prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix.
     *
     * @param newPrefix the new prefix
     * @throws MalformedURLException the malformed url exception
     */
    public void setPrefix(String newPrefix) throws MalformedURLException {
        this.setPrefix(this.prefix, newPrefix);
    }

    @Override
    public void setPrefix(String oldPrefix, String newPrefix) throws MalformedURLException {
        if (newPrefix != null && newPrefix.length() > 0) {
            for (ZestStatement stmt : this.statements) {
                stmt.setPrefix(oldPrefix, newPrefix);
            }
        }
        this.prefix = newPrefix;
    }

    /**
     * Gets the parameters.
     *
     * @return the parameters
     */
    public ZestVariables getParameters() {
        return this.parameters;
    }

    /**
     * Sets the parameters.
     *
     * @param parameters the new parameters
     */
    public void setParameters(ZestVariables parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets the zest version.
     *
     * @return the zest version
     */
    public String getZestVersion() {
        return zestVersion;
    }

    /**
     * Sets the zest version.
     *
     * @param zestVersion the new zest version
     */
    public void setZestVersion(String zestVersion) {
        if (!VERSION.equals(zestVersion) || !VERSION.equals("0.8")) {
            throw new IllegalArgumentException(
                    "Version " + zestVersion + " not supported by this class");
        }
        this.zestVersion = zestVersion;
    }

    /**
     * Gets the generated by.
     *
     * @return the generated by
     */
    public String getGeneratedBy() {
        return generatedBy;
    }

    /**
     * Sets the generated by.
     *
     * @param generatedBy the new generated by
     */
    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    /**
     * Gets the about.
     *
     * @return the about
     */
    public String getAbout() {
        return about;
    }

    /**
     * Sets the about.
     *
     * @param about the new about
     */
    public void setAbout(String about) {
        this.about = about;
    }

    /**
     * Gets the author.
     *
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author.
     *
     * @param author the new author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public Set<String> getVariableNames() {
        Set<String> tokens = new HashSet<>();

        // Add the 'standard' ones
        tokens.add(ZestVariables.REQUEST_URL);
        tokens.add(ZestVariables.REQUEST_METHOD);
        tokens.add(ZestVariables.REQUEST_HEADER);
        tokens.add(ZestVariables.REQUEST_BODY);
        tokens.add(ZestVariables.RESPONSE_URL);
        tokens.add(ZestVariables.RESPONSE_HEADER);
        tokens.add(ZestVariables.RESPONSE_BODY);

        List<String[]> vars = this.getParameters().getVariables();
        for (String[] var : vars) {
            tokens.add(var[0]);
        }

        ZestStatement next = this.getNext();
        while (next != null) {
            if (next instanceof ZestAssignment) {
                tokens.add(((ZestAssignment) next).getVariableName());
            } else if (next instanceof ZestClientAssignCookie) {
                tokens.add(((ZestClientAssignCookie) next).getVariableName());
            } else if (next instanceof ZestClientElementAssign) {
                tokens.add(((ZestClientElementAssign) next).getVariableName());
            } else if (next instanceof ZestLoop) {
                tokens.add(((ZestLoop<?>) next).getVariableName());
            }
            next = next.getNext();
        }

        return tokens;
    }

    /**
     * Returns a set containing all of the window handles defined in this script
     *
     * @return the window handles.
     */
    public Set<String> getClientWindowHandles() {
        Set<String> ids = new HashSet<>();
        ZestStatement next = this.getNext();
        while (next != null) {
            if (next instanceof ZestClientLaunch) {
                ids.add(((ZestClientLaunch) next).getWindowHandle());
            } else if (next instanceof ZestClientWindowHandle) {
                ids.add(((ZestClientWindowHandle) next).getWindowHandle());
            }
            next = next.getNext();
        }
        return ids;
    }

    @Override
    public int getIndex(ZestStatement child) {
        return this.statements.indexOf(child);
    }

    @Override
    public ZestStatement getLast() {
        return null;
    }

    @Override
    public ZestStatement getChildBefore(ZestStatement child) {
        if (this.statements.contains(child)) {
            int childIndex = this.statements.indexOf(child);
            if (childIndex > 1) {
                return this.statements.get(childIndex - 1);
            }
        }
        return null;
    }

    @Override
    public boolean isPassive() {
        return Type.Passive.equals(scriptType);
    }

    private void checkStatementIndexes() {
        /* Only use when debugging index issues
        Map<Integer, ZestStatement>  map = new HashMap<Integer, ZestStatement>();
        for (ZestStatement statement : statements) {
        	ZestStatement stmt = map.put(statement.getIndex(), statement);
        	if (stmt != null) {
        		System.out.println("2 statements with the same index:( " + stmt.getIndex());
        		(new Exception()).printStackTrace();
        	}
        }
        */
    }

    @Override
    protected ZestStatement setPrev(ZestStatement prev) {
        for (ZestStatement statement : statements) {
            if (prev != null) {
                prev.setNext(statement);
            }
            prev = statement.setPrev(prev);
        }
        return prev;
    }
}
