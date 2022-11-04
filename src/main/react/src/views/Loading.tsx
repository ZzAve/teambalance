import React, { useEffect, useState } from "react";
import { SpinnerWithText } from "../components/SpinnerWithText";

const Texts = [
  "Netten aan het opspannen",
  "Muntjes poetsen",
  "🏐 TOVO TOVO 🏐 🥇",
  "De fluim van de maand verzinnen",
  "De eentjes van de nulletjes scheiden",
  "Wachtwoord controleren",
  "💵 💲 💰", // emojis
];

const Loading = () => {
  const [isUpdating, setIsUpdating] = useState(false);
  const [text, setText] = useState("Inloggen ...");
  const [lastDelayedExecution, setLastDelayedExecution] =
    useState<NodeJS.Timeout>(setTimeout(() => {}));

  // Unmount cancellation effect
  useEffect(() => {
    return () => {
      clearTimeout(lastDelayedExecution);
    };
  }, [lastDelayedExecution]);

  useEffect(() => {
    if (isUpdating) return;

    const updateTextHandle: NodeJS.Timeout = updateText();
    setLastDelayedExecution(updateTextHandle);
    setIsUpdating(true);
  }, [isUpdating]);

  const updateText = () =>
    setTimeout(() => {
      if (Math.random() < 0.4) {
        let index = Math.floor(Math.random() * Texts.length);
        setText(`${Texts[index]} ...`);
      }

      setIsUpdating(false);
    }, 750);

  return <SpinnerWithText text={text} size={"lg"} />;
};

export default Loading;
