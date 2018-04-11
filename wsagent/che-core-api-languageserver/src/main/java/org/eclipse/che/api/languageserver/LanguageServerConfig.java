/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Language server internal configuration that is used by corresponding components for the
 * definition of language server instance launch, initialization, matching procedures, while it also
 * specify language server specific configuration elements (like file watch patterns, etc.)
 *
 * @see LanguageServerConfigProvider
 * @see LanguageServerConfigProcessor
 */
public interface LanguageServerConfig {

  /**
   * Get a language server regular expression provider.
   *
   * @return regular expression provider
   */
  RegexProvider getRegexpProvider();

  /**
   * Get a language server communication provider. If there is no need for a custom communication
   * provider, there is a default implementation {@link EmptyCommunicationProvider}.
   *
   * @return communication provider
   * @see EmptyCommunicationProvider
   */
  CommunicationProvider getCommunicationProvider();

  /**
   * Get a language server instance provider. If there is no need for a custom instance provider,
   * there is a default implementation {@link DefaultInstanceProvider}
   *
   * @return instance provider
   * @see DefaultInstanceProvider
   */
  InstanceProvider getInstanceProvider();

  /**
   * Shows if language server is local (e.g. running as a process) or remote (e.g. running as a
   * remote server)
   *
   * @return true if local, false if remote
   */
  default boolean isLocal() {
    return true;
  }

  /**
   * Instance provider is the definition of the way a language server instance is constructed. It is
   * not restricted to provide an instance in any possible way, however the library that is uses
   * <code>lsp4j</code> imposes its restrictions. The implementer must use {@link LanguageClient}
   * and {@link LanguageServer} interfaces correspondingly.
   *
   * @see DefaultInstanceProvider
   */
  interface InstanceProvider {
    /**
     * Get a new language server instance for specified communication means.
     *
     * @param client language server client
     * @param in language server input stream
     * @param out language server output stream
     * @return language server instance
     */
    LanguageServer get(LanguageClient client, InputStream in, OutputStream out);
  }

  /**
   * Provides some way we can communicate with the running language server. In general the
   * implementer has no obligation as of how to launch/initialize local or remote language server
   * process/server, it can be done separately and it is not required that this interface covers the
   * procedure, however it is also not disallowed.
   *
   * <p>{@link SocketCommunicationProvider} and {@link ProcessCommunicationProvider} implementations
   * of the interface, besides providing communication means establishes a socket connection and
   * launches a language server process correspondingly.
   *
   * @see EmptyCommunicationProvider
   * @see SocketCommunicationProvider
   * @see ProcessCommunicationProvider
   */
  interface CommunicationProvider {
    /**
     * Get input stream
     *
     * @return input stream
     * @throws LanguageServerException if any issue happened trying to get an input stream
     */
    InputStream getInputStream() throws LanguageServerException;

    /**
     * Get output stream
     *
     * @return output stream
     * @throws LanguageServerException if any issue happened trying to get an output stream
     */
    OutputStream getOutputStream() throws LanguageServerException;

    /**
     * Get status checker
     *
     * @return status checker
     * @throws LanguageServerException if any issue happened trying to get a status checker
     */
    StatusChecker getStatusChecker() throws LanguageServerException;

    /**
     * The implementation must provide aliveness status for language server that we communicate to.
     */
    interface StatusChecker {
      /**
       * Shows if a language server communication is alive
       *
       * @return true - if communication is alive, false - if communication is not alive
       */
      boolean isAlive();

      /**
       * Get cause of language server communication not being alive.
       *
       * @return short description of why the communication is not alive, if the communication is
       *     alive (<code>isAlive == true</code>) returns <code>null</code>.
       */
      String getCause();
    }
  }

  /** Provides language server matching regular expression definitions. */
  interface RegexProvider {
    /**
     * Get a map of <code>languageId->languageRegexp</code> where <code>languageId</code> is the
     * identifier of a language that is listed in the <a
     * href=https://microsoft.github.io/language-server-protocol/specification>language server
     * specification</a>, while <code>languageRegexp</code> is a regular expression that matches all
     * files that are related to a corresponding language. This map is used to determine which file
     * belongs to which language server.
     *
     * @return map of language regexes
     */
    Map<String, String> getLanguageRegexes();

    /**
     * Get a file watch patterns for a {@link LanguageServerFileWatcher}
     *
     * @return set of stringified file watch patterns
     */
    Set<String> getFileWatchPatterns();
  }
}
