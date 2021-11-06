import React from "react";
import { Helmet } from "react-helmet";


const PageTitle = ({title = "", withSuffix= true}) => (
        <Helmet>
            <title>{title}{withSuffix ? " - Team balance": ""}</title>
        </Helmet>
);

export default PageTitle;

