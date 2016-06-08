/*
 * DownloadClient Geodateninfrastruktur Bayern
 *
 * (c) 2016 GSt. GDI-BY (gdi.bayern.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bayern.gdi.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/** 'Verarbeitungsschritt' of processing step configuration. */
@XmlRootElement(name = "Verarbeitungsschritt")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessingStepConfiguration {

    @XmlElement(name = "Titel")
    private String title;

    @XmlElement(name = "Beschreibung")
    private String description;

    @XmlElement(name = "Befehl")
    private String command;

    @XmlElementWrapper(name = "ParameterSet")
    @XmlElement(name = "Parameter")
    private List<ConfigurationParameter> parameters;

    public ProcessingStepConfiguration() {
        parameters = new ArrayList<>();
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * @param command the command to set
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * @return the parameters
     */
    public List<ConfigurationParameter> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(List<ConfigurationParameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return this.title;
    }
}
