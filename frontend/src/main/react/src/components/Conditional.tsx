import React from "react";

export const Conditional: (props: {
  condition: boolean;
  children: JSX.Element | Array<JSX.Element>;
}) => JSX.Element = (props: {
  condition: boolean;
  children: JSX.Element | Array<JSX.Element>;
}) => (props.condition ? <>{props.children}</> : <></>);

export default Conditional;
