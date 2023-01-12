import React from "react";
import { Helmet } from "react-helmet";

const PageTitle = (props: { title?: string; withSuffix?: boolean }) => {
  const { title = "", withSuffix = true } = props;
  return (
    <Helmet>
      <title>
        {title}
        {withSuffix ? " - Team balance" : ""}
      </title>
    </Helmet>
  );
};

export default PageTitle;
