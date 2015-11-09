import _ from 'underscore';
import React from 'react';
import SeverityHelper from '../../../components/shared/severity-helper';
import { DomainHeader } from '../domain/header';
import { getComponentIssuesUrl } from '../../../helpers/urls';
import { formatMeasure } from '../../../helpers/measures';


export default class extends React.Component {
  sortedSeverities () {
    return _.sortBy(this.props.severities, s => window.severityComparator(s.val));
  }

  render () {
    let rows = this.sortedSeverities().map(s => {
      let href = getComponentIssuesUrl(this.props.component.key, { resolved: 'false', severities: s.val });
      return <tr key={s.val}>
        <td>
          <SeverityHelper severity={s.val}/>
        </td>
        <td className="thin text-right">
          <a className="cell-link" href={href}>
            {formatMeasure(s.count, 'SHORT_INT')}
          </a>
        </td>
      </tr>;
    });

    return <div className="overview-domain-section">
      <DomainHeader title="Prioritized Issues"/>
      <table className="data zebra">
        <tbody>{rows}</tbody>
      </table>
    </div>;
  }
}