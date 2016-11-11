/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import React from 'react';
import keyBy from 'lodash/keyBy';
import LoginSection from './LoginSection';
import LoginForm from './LoginForm';
import DropImage from './DropImage';
import AboutProjects from './AboutProjects';
import AboutIssues from './AboutIssues';
import AboutScanners from './AboutScanners';
import { translate } from '../../../helpers/l10n';
import '../styles.css';
import { searchProjects } from '../../../api/components';
import { getFacet } from '../../../api/issues';

const links = {
  leak: 'http://docs.sonarqube.org/display/HOME/Fixing+the+Water+Leak',
  qualityGates: 'http://docs.sonarqube.org/display/SONAR/Quality+Gates',
  rules: 'http://docs.sonarqube.org/display/SONAR/Rules'
};

export default class AboutApp extends React.Component {
  state = {
    loading: true
  };

  componentDidMount () {
    this.mounted = true;
    this.loadData();
  }

  componentWillUnmount () {
    this.mounted = false;
  }

  loadProjects () {
    return searchProjects({ ps: 1 }).then(r => r.paging.total);
  }

  loadIssues () {
    return getFacet({ resolved: false }, 'types').then(r => keyBy(r.facet, 'val'));
  }

  loadData () {
    Promise.all([
      window.sonarqube.appStarted,
      this.loadProjects(),
      this.loadIssues()
    ]).then(responses => {
      if (this.mounted) {
        const [options, projectsCount, issueTypes] = responses;
        this.setState({
          projectsCount,
          issueTypes,
          logoUrl: options.logoUrl,
          logoWidth: options.logoWidth,
          loading: false
        });
      }
    });
  }

  render () {
    if (this.state.loading) {
      return null;
    }

    const isAuthenticated = !!window.SS.user;
    const loginFormShown = !isAuthenticated && this.props.location.query.login !== undefined;

    const logoUrl = this.state.logoUrl || `${window.baseUrl}/images/logo.svg`;
    const logoWidth = this.state.logoWidth || 100;
    const logoHeight = 30;
    const logoTitle = this.state.logoUrl ? '' : translate('layout.sonar.slogan');

    return (
        <div id="about-page" className="about-page">
          <div className="about-page-entry">

            <div className="about-page-logo">
              <img src={logoUrl} width={2 * logoWidth} height={2 * logoHeight} alt={logoTitle}/>
            </div>

            {loginFormShown ? (
                <div className="about-page-entry-box">
                  <LoginForm/>
                </div>
            ) : (
                <div className="about-page-entry-box">
                  <AboutProjects count={this.state.projectsCount}/>
                  {!isAuthenticated && <LoginSection/>}
                </div>
            )}
          </div>

          <div className="about-page-section about">
            <div className="about-page-center-container">
              <h2 className="about-page-header">Keep your code clean by fixing the leak</h2>
              <p className="about-page-text about-page-text-center">
                By fixing new issues as they appear in code, you create and maintain a clean code base.
                <br/>
                Even on legacy projects, focusing on keeping new code clean will eventually yield a code base you can be
                proud of.
              </p>
              <div className="about-page-section-image">
                <DropImage/>
              </div>
            </div>
          </div>

          <AboutIssues
              bugs={this.state.issueTypes['BUG'].count}
              vulnerabilities={this.state.issueTypes['VULNERABILITY'].count}
              codeSmells={this.state.issueTypes['CODE_SMELL'].count}/>

          <div className="about-page-section">
            <div className="about-page-container clearfix">
              <img className="pull-right" src="http://placehold.it/500x175" width={500} height={175} alt=""/>
              <h2 className="about-page-header">Understanding Quality Gates</h2>
              <p className="about-page-text">
                Your project's quality gate is the set of conditions the project must meet before it can be released
                into production. The quality gate is designed to ensure that the next version's quality will be better
                than the last.
              </p>
              <div className="big-spacer-top">
                <a href={links.qualityGates} target="_blank">Read more</a>
              </div>
            </div>
          </div>

          <div className="about-page-section">
            <div className="about-page-container clearfix">
              <img className="pull-left" src="http://placehold.it/500x175" width={500} height={175} alt=""/>
              <h2 className="about-page-header">Understanding the Leak Period</h2>
              <p className="about-page-text">
                The leak metaphor and the default Quality Gate are based on the leak period - the recent period against
                which you're tracking issues. For some <code>previous_version</code> makes the most sense, for others
                the last 30 days is a good option.
              </p>
              <div className="big-spacer-top">
                <a href={links.leak} target="_blank">Read more</a>
              </div>
            </div>
          </div>

          <div className="about-page-section">
            <div className="about-page-container clearfix">
              <img className="pull-right" src="http://placehold.it/500x175" width={500} height={175} alt=""/>
              <h2 className="about-page-header">Conform to recognized standards</h2>
              <p className="about-page-text">
                SonarAnalyzers offer rules that support industry standards: MISRA, CERT, CWE, OWASP Top 10 and SANS Top
                25. Configure your Quality Profile with standard-related rules to ensure adherence.
              </p>
              <div className="big-spacer-top">
                <a href={links.rules} target="_blank">Read more</a>
              </div>
            </div>
          </div>

          <AboutScanners/>
        </div>
    );
  }
}
